/* Copyright 2013 The jeo project. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jeo.geojson.parser;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import io.jeo.geom.Geom;
import io.jeo.json.parser.ParseException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.PackedCoordinateSequenceFactory;

public class GeometryHandler extends BaseHandler {

    GeometryFactory gf = new GeometryFactory();

    @Override
    public boolean startObject() throws ParseException, IOException {
        return true;
    }
    
    @Override
    public boolean endObject() throws ParseException, IOException {
        Geometry g = null;

        List geometries = node.consume("geometries", List.class).orElse(null);
        if (geometries != null) {
            g = createGeometryCollection(geometries);
        }
        else {
            String type = node.consume("type", String.class).orElse(null);
            List coordinates = node.consume("coordinates", List.class).orElse(null);
            g = createGeometry(type, coordinates);
        }
        node.setValue(g);

        //up();
        pop();

        return true;
    }

    @Override
    public boolean startObjectEntry(String key) throws ParseException, IOException {
        if ("type".equals(key)) {
            push(key, new TypeHandler());
        }
        else if ("coordinates".equals(key)) {
            push(key, new CoordinateHandler());
        }
        else if ("geometries".equals(key)) {
            push(key, new GeometryCollectionHandler());
        }
        return true;
    }
    
    @Override
    public boolean endObjectEntry() throws ParseException, IOException {
        return true;
    }

    @Override
    public boolean primitive(Object value) throws ParseException, IOException {
        node.find("type").setValue(value);
        return true;
    }

    Geometry createGeometry(String type, List coordinates) {
        switch(Geom.Type.from(type)) {
        case POINT:
            return createPoint(coordinates);
        case LINESTRING:
            return createLineString(coordinates);
        case POLYGON:
            return createPolygon(coordinates);
        case MULTIPOINT:
            return createMultiPoint(coordinates);
        case MULTILINESTRING:
            return createMultiLineString(coordinates);
        case MULTIPOLYGON:
            return createMultiPolygon(coordinates);
        default:
            throw new IllegalArgumentException("Unexpected geometry type: " + type);
        }
    }

    Point createPoint(List list) {
        return gf.createPoint(coord(list));
    }

    LineString createLineString(List list) {
        return gf.createLineString(coordseq(list));
    }

    Polygon createPolygon(List list) {
        LinearRing shell = gf.createLinearRing(coordseq((List)ensureSize(list, 1).get(0)));
        LinearRing[] holes = list.size() > 1 ? new LinearRing[list.size()-1] : null;

        for (int i = 1; i < list.size(); i++) {
            holes[i-1] = gf.createLinearRing(coordseq((List) list.get(i))); 
        }
        return gf.createPolygon(shell, holes);
    }

    MultiPoint createMultiPoint(List list) {
        return gf.createMultiPoint(coordseq(list));
    }

    MultiLineString createMultiLineString(List list) {
        LineString[] lines =  new LineString[ensureSize(list, 1).size()];
        for (int i = 0; i < list.size(); i++) {
            lines[i] = createLineString((List) list.get(i));
        }
        return gf.createMultiLineString(lines);
    }

    MultiPolygon createMultiPolygon(List list) {
        Polygon[] polys =  new Polygon[ensureSize(list, 1).size()];
        for (int i = 0; i < list.size(); i++) {
            polys[i] = createPolygon((List) list.get(i));
        }
        return gf.createMultiPolygon(polys);
    }

    GeometryCollection createGeometryCollection(List geoms) {
        return gf.createGeometryCollection((Geometry[])geoms.toArray(new Geometry[geoms.size()]));
    }

    Coordinate coord(List list) {
        ensureSize(list, 2);

        double x = number(list.get(0));
        double y = number(list.get(1));
        double z = list.size() > 2 ? number(list.get(2)) : Double.NaN;

        Coordinate c = new Coordinate(x, y);
        if (!Double.isNaN(z)) {
            c.z = z;
        }
        return c;
    }

    CoordinateSequence coordseq(List list) {
        ensureSize(list, 1);

        int dim = ensureSize((List) list.get(0), 2).size();
        
        CoordinateSequence seq =
            PackedCoordinateSequenceFactory.DOUBLE_FACTORY.create(list.size(), dim);

        for (int i = 0; i < list.size(); i++) {
            List c = (List) list.get(i);
            seq.setOrdinate(i, 0, number(c.get(0)));
            seq.setOrdinate(i, 1, number(c.get(1)));

            if (dim > 2) {
                seq.setOrdinate(i, 2, number(c.get(2)));
            }
        }

        return seq;
    }

    double number(Object obj) {
        return ((Number)obj).doubleValue();
    }

    List ensureSize(List list, int size) {
        if (list.size() < size) {
            throw new IllegalArgumentException(String.format(Locale.ROOT,
                "expected coordinate arary of size %d but is of size %d", size, list.size()));
        }
        return list;
    }

    static class GeometryCollectionHandler extends BaseHandler {
    
        @Override
        public boolean startArray() throws ParseException, IOException {
            return true;
        }

        @Override
        public boolean endArray() throws ParseException, IOException {
            List<Geometry> geoms = node.consumeAll("geometry", Geometry.class);
            node.setValue(geoms);

            pop();
            return true;
        }

        @Override
        public boolean startObject() throws ParseException, IOException {
            push("geometry", new GeometryHandler());
            return true;
        }
    }
}
