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
package io.jeo.mongo;

import io.jeo.geom.Geom;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
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

public class GeoJSON {

    static DBObject toObject(Geometry g) {
        if (g == null) {
            return null;
        }
    
        Geom.Type type = Geom.Type.from(g);
        switch(type) {
        case POINT:
            return toObject((Point)g);
        case LINESTRING:
            return toObject((LineString)g);
        case POLYGON:
            return toObject((Polygon)g);
        case MULTIPOINT:
            return toObject((MultiPoint)g);
        case MULTILINESTRING:
            return toObject((MultiLineString)g);
        case MULTIPOLYGON:
            return toObject((MultiPolygon)g);
        case GEOMETRYCOLLECTION:
            return toObject((GeometryCollection)g);
        default:
            throw new IllegalArgumentException("Unsupported geometry: " + g);
        }
    }
    
    static Geometry toGeometry(DBObject obj) {
        if (obj == null) {
            return null;
        }
    
        String type = (String) obj.get("type");
        if (type == null) {
            throw new IllegalArgumentException("Object " + obj + " has no type property");
        }
    
        switch(Geom.Type.from(type)) {
        case POINT:
            return toPoint(obj);
        case LINESTRING:
            return toLineString(obj);
        case POLYGON:
            return toPolygon(obj);
        case MULTIPOINT:
            return toMultiPoint(obj);
        case MULTILINESTRING:
            return toMultiLineString(obj);
        case MULTIPOLYGON:
            return toMultiPolygon(obj);
        case GEOMETRYCOLLECTION:
            return toCollection(obj);
        default:
            throw new IllegalArgumentException("Unsupported geometry type: " + type);
        }
    }
    
    static DBObject toObject(Point p) {
        BasicDBList coords = new BasicDBList();
        coords.add(p.getX());
        coords.add(p.getY());
    
        BasicDBObject obj = new BasicDBObject("type", "Point");
        obj.put("coordinates", coords);
        return obj;
    }
    
    static Point toPoint(DBObject obj) {
        BasicDBList list = (BasicDBList) obj.get("coordinates");
        return new GeometryFactory().createPoint(toCoordinate(list));
    }
    
    static DBObject toObject(LineString l) {
        BasicDBObject obj = new BasicDBObject("type", "LineString");
        obj.put("coordinates", toList(l.getCoordinateSequence()));
        return obj;
    }
    
    static LineString toLineString(DBObject obj) {
        BasicDBList list = (BasicDBList) obj.get("coordinates");
        return new GeometryFactory().createLineString(toCoordinates(list));
    }
    
    static DBObject toObject(Polygon p) {
        BasicDBList coords = new BasicDBList();
        coords.add(toList(p.getExteriorRing().getCoordinateSequence()));
        for (int i = 0; i < p.getNumInteriorRing(); i++) {
            coords.add(toList(p.getInteriorRingN(i).getCoordinateSequence()));
        }
        BasicDBObject obj = new BasicDBObject("type", "Polygon");
        obj.put("coordinates", coords);
        return obj;
    }
    
    static Polygon toPolygon(DBObject obj) {
        BasicDBList list = (BasicDBList) obj.get("coordinates");
        return toPolygon(list);
    }
    
    static DBObject toObject(MultiPoint mp) {
        BasicDBObject obj = new BasicDBObject("type", "MultiPoint");
    
        BasicDBList coords = new BasicDBList();
        for (Coordinate c : mp.getCoordinates()) {
            BasicDBList coord = new BasicDBList();
            coord.add(c.x);
            coord.add(c.y);
            coords.add(coord);
        }
        return obj;
    }
    
    static MultiPoint toMultiPoint(DBObject obj) {
        BasicDBList list = (BasicDBList) obj.get("coordinates");
        return new GeometryFactory().createMultiPoint(toCoordinates(list));
    }
    
    static DBObject toObject(MultiLineString ml) {
        BasicDBObject obj = new BasicDBObject("type", "MultiLineString");
        BasicDBList coords = new BasicDBList();
        for (LineString l : Geom.iterate(ml)) {
            coords.add(toList(l.getCoordinateSequence()));
        }
        obj.put("coordinates", coords);
        return obj;
    }
    
    static MultiLineString toMultiLineString(DBObject obj) {
        GeometryFactory gf = new GeometryFactory();
    
        BasicDBList list = (BasicDBList) obj.get("coordinates");
        LineString[] lines = new LineString[list.size()];
    
        for (int i = 0; i < list.size(); i++) {
            lines[i] = gf.createLineString(toCoordinates((BasicDBList)list.get(i)));
        }
    
        return gf.createMultiLineString(lines);
    }
    
    static DBObject toObject(MultiPolygon mp) {
        BasicDBObject obj = new BasicDBObject("type", "MultiPolygon");
        BasicDBList coords = new BasicDBList();
        for (Polygon p : Geom.iterate(mp)) {
            BasicDBList poly = new BasicDBList();
            poly.add(toList(p.getExteriorRing().getCoordinateSequence()));
            for (int i = 0; i < p.getNumInteriorRing(); i++) {
                poly.add(toList(p.getInteriorRingN(i).getCoordinateSequence()));
            }
            coords.add(poly);
        }
        obj.put("coordinates", coords);
        return obj;
    }
    
    static MultiPolygon toMultiPolygon(DBObject obj) {
        GeometryFactory gf = new GeometryFactory();
    
        BasicDBList list = (BasicDBList) obj.get("coordinates");
        Polygon[] polys = new Polygon[list.size()];
        
        for (int i = 0; i < list.size(); i++) {
            polys[i] = toPolygon((BasicDBList)list.get(i));
        }
    
        return gf.createMultiPolygon(polys);
    }
    
    static DBObject toObject(GeometryCollection gc) {
        BasicDBObject obj = new BasicDBObject("type", "GeometryCollection");
        BasicDBList geoms = new BasicDBList();
    
        for (Geometry g : Geom.iterate(gc)) {
            geoms.add(toObject(g));
        }
        return obj;
    }
    
    static GeometryCollection toCollection(DBObject obj) {
        BasicDBList list = (BasicDBList) obj.get("geometries");
    
        Geometry[] geoms = new Geometry[list.size()];
    
        for (int i = 0; i < list.size(); i++) {
            geoms[i] = toGeometry((DBObject) list.get(i));
        }
    
        return new GeometryFactory().createGeometryCollection(geoms);
    }
    
    static BasicDBList toList(CoordinateSequence coordseq) {
        BasicDBList list = new BasicDBList();
    
        for (int i = 0; i < coordseq.size(); i++) {
            BasicDBList sub = new BasicDBList();
            sub.add(coordseq.getOrdinate(i, 0));
            sub.add(coordseq.getOrdinate(i, 1));
            if (coordseq.getDimension() > 2) {
                double z = coordseq.getOrdinate(i, 2);
                if (!Double.isNaN(z)) {
                   sub.add(z);
                }
            }
            list.add(sub);
        }
    
        return list;
    }
    
    static Coordinate toCoordinate(BasicDBList list) {
        return new Coordinate(((Number)list.get(0)).doubleValue(), ((Number)list.get(1)).doubleValue());
    }
    
    static Coordinate[] toCoordinates(BasicDBList list) {
        Coordinate[] coords = new Coordinate[list.size()];
        for (int i = 0; i < list.size(); i++) {
            coords[i] = toCoordinate( (BasicDBList) list.get(i));
        }
        return coords;
    }
    
    static Polygon toPolygon(BasicDBList list) {
        GeometryFactory gf = new GeometryFactory();
    
        LinearRing shell = gf.createLinearRing(toCoordinates((BasicDBList) list.get(0)));
    
        LinearRing[] holes = list.size() > 1 ? new LinearRing[list.size()-1] : null;
        for (int i = 1; i < list.size(); i++) {
            holes[i-1] = gf.createLinearRing(toCoordinates((BasicDBList) list.get(i)));
        }
    
        return new GeometryFactory().createPolygon(shell, holes);
    }
}
