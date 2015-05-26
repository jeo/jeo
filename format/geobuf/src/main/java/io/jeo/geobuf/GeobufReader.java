/* Copyright 2015 The jeo project. All rights reserved.
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
package io.jeo.geobuf;

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.PackedCoordinateSequenceFactory;
import io.jeo.data.Disposable;
import io.jeo.geobuf.Geobuf.Data;
import io.jeo.geobuf.Geobuf.Data.DataTypeCase;
import io.jeo.geobuf.Geobuf.Data.Feature;
import io.jeo.geobuf.Geobuf.Data.Feature.IdTypeCase;
import io.jeo.geobuf.Geobuf.Data.FeatureCollection;
import io.jeo.geobuf.Geobuf.Data.Geometry;
import io.jeo.geobuf.Geobuf.Data.Value;
import io.jeo.geobuf.Geobuf.Data.Value.ValueTypeCase;
import io.jeo.proj.Proj;
import io.jeo.vector.BasicFeature;
import io.jeo.vector.FeatureCursor;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads a geobuf protocol buffer stream.
 */
public class GeobufReader implements Disposable {

    static Logger LOG = LoggerFactory.getLogger(GeobufReader.class);

    // raw input
    final InputStream in;

    // the geobuf payload
    final Data.Builder data;

    // property keys
    final List<String> keys;

    // coordinate dimensions
    final int dim;

    // precision ^ 10
    final double e;

    // coordinate / geometry factories
    final PackedCoordinateSequenceFactory csFactory;
    final GeometryFactory gFactory;

    public GeobufReader(InputStream in) throws IOException {
        this.in = in;

        data = Data.newBuilder();
        data.mergeFrom(in);

        keys = data.getKeysList();

        dim = data.getDimensions();
        e = Math.pow(10, data.getPrecision());

        csFactory = new PackedCoordinateSequenceFactory(PackedCoordinateSequenceFactory.DOUBLE, dim);
        gFactory = new GeometryFactory(csFactory);
    }

    public com.vividsolutions.jts.geom.Geometry geometry() {
        DataTypeCase type = data.getDataTypeCase();
        if (type != DataTypeCase.GEOMETRY) {
            throw new IllegalArgumentException("Geobuf data type: " + type + ", not geometry");
        }

        return decode(data.getGeometry());
    }

    public Point point() {
        return (Point) geometry();
    }

    public LineString lineString() {
        return (LineString) geometry();
    }

    public Polygon polygon() {
        return (Polygon) geometry();
    }

    public MultiPoint multiPoint() {
        return (MultiPoint) geometry();
    }

    public MultiLineString multiLineString() {
        return (MultiLineString) geometry();
    }

    public MultiPolygon multiPolygon() {
        return (MultiPolygon) geometry();
    }

    public io.jeo.vector.Feature feature() {
        DataTypeCase type = data.getDataTypeCase();
        if (type != DataTypeCase.FEATURE) {
            throw new IllegalArgumentException("Geobuf data type: " + type + ", not feature");
        }

        return decode(data.getFeature());
    }

    public FeatureCursor featureCollection() {
        DataTypeCase type = data.getDataTypeCase();
        if (type != DataTypeCase.FEATURE_COLLECTION) {
            throw new IllegalArgumentException("Geobuf data type: " + type + ", not feature collection");
        }

        return decode(data.getFeatureCollection());
    }

    public CoordinateReferenceSystem crs() {
        final List<Integer> props;
        final List<Value> vals;
        switch(data.getDataTypeCase()) {
            case GEOMETRY:
                props = data.getGeometry().getCustomPropertiesList();
                vals = data.getGeometry().getValuesList();
                break;
            case FEATURE:
                props = data.getFeature().getCustomPropertiesList();
                vals = data.getFeature().getValuesList();
                break;
            case FEATURE_COLLECTION:
                props = data.getFeatureCollection().getCustomPropertiesList();
                vals = data.getFeatureCollection().getValuesList();
                break;
            default:
                return null;
        }

        int i = data.getKeysList().indexOf(CustomKeys.CRS);
        int p = props.indexOf(i);
        if (p > -1 && p % 2 == 0) {
            return Proj.crs((String) decode(vals.get(props.get(p+1))));
        }
        return null;
    }

    com.vividsolutions.jts.geom.Geometry decode(Geometry g) {
        switch (g.getType()) {
            case POINT:
                return decodePoint(g);
            case LINESTRING:
                return decodeLine(g);
            case POLYGON:
                return decodePolygon(g);
            case MULTIPOINT:
                return decodeMultiPoint(g);
            case MULTILINESTRING:
                return decodeMultiLine(g);
            case MULTIPOLYGON:
                return decodeMultiPolygon(g);
            case GEOMETRYCOLLECTION:
                return decodeCollection(g);
            default:
                throw new UnsupportedOperationException();
        }
    }

    com.vividsolutions.jts.geom.Point decodePoint(Geometry g) {
        double[] p = new double[dim];
        for (int k = 0; k < dim; k++) {
            p[k] = g.getCoords(k) / e;
        }

        return gFactory.createPoint(csFactory.create(p, dim));
    }

    com.vividsolutions.jts.geom.LineString decodeLine(Geometry g) {
        return gFactory.createLineString(readAllCoords(g, false));
    }

    com.vividsolutions.jts.geom.LineString decodeLine(Geometry g, int start, int len) {
        return gFactory.createLineString(readCoords(g, start, len, false));
    }

    com.vividsolutions.jts.geom.LinearRing

    decodeRing(Geometry g, int start, int len) {
        return gFactory.createLinearRing(readCoords(g, start, len, true));
    }

    com.vividsolutions.jts.geom.Polygon decodePolygon(Geometry g) {
        if (g.getLengthsCount() > 0) {
            return decodePolygon(g, 0, 0, g.getLengthsCount());
        }
        else {
            return gFactory.createPolygon(readAllCoords(g, true));
        }
    }

    com.vividsolutions.jts.geom.Polygon decodePolygon(Geometry g, int start, int lenStart, int nRings) {
        int len = g.getLengths(lenStart);
        LinearRing shell = decodeRing(g, start, len);

        LinearRing[] holes = new LinearRing[nRings-1];
        for (int i = 0; i < holes.length; i++) {
            start += len;
            len = g.getLengths(lenStart+i+1);
            holes[i] = decodeRing(g, start, len);
        }

        return gFactory.createPolygon(shell, holes);
    }

    com.vividsolutions.jts.geom.MultiPoint decodeMultiPoint(Geometry g) {
        return gFactory.createMultiPoint(readCoords(g, 0, g.getCoordsCount()/dim, false));
    }

    com.vividsolutions.jts.geom.MultiLineString decodeMultiLine(Geometry g) {
        LineString[] lines;
        if (g.getLengthsCount() > 0) {
            lines = new LineString[g.getLengthsCount()];

            int start = 0;
            for (int i = 0; i < lines.length; i++) {
                int len = g.getLengths(i);
                lines[i] = decodeLine(g, start, len);
                start += len;
            }
        }
        else {
            lines = new LineString[]{ decodeLine(g) };
        }

        return gFactory.createMultiLineString(lines);
    }

    com.vividsolutions.jts.geom.MultiPolygon decodeMultiPolygon(Geometry g) {
        Polygon[] polygons;
        if (g.getLengthsCount() > 0) {
            List<Polygon> list = new ArrayList<>(g.getLengths(0));
            int start = 0;
            int i = 1;
            while (i < g.getLengthsCount()) {
                int nrings = g.getLengths(i++);
                list.add(decodePolygon(g, start, i, nrings));

                for (int j = 0; j < nrings; j++) {
                    start += g.getLengths(i);
                    i++;
                }
            }

            polygons = list.toArray(new Polygon[list.size()]);
        }
        else {
            polygons = new Polygon[]{decodePolygon(g)};
        }
        return gFactory.createMultiPolygon(polygons);
    }

    com.vividsolutions.jts.geom.Geometry decodeCollection(Geometry g) {
        List<com.vividsolutions.jts.geom.Geometry> geoms = new ArrayList<>();
        if (g.getGeometriesCount() < 2) {
            geoms.add(decode(g));
        }
        else {
            for (int i = 0; i < g.getGeometriesCount(); i++) {
                geoms.add(decode(g.getGeometries(i)));
            }
        }

        return gFactory.buildGeometry(geoms);
    }

    io.jeo.vector.Feature decode(Feature f) {
        Map<String,Object> values = new LinkedHashMap<>();

        // geometry
        values.put("geometry", decode(f.getGeometry()));

        // properties
            for (int i = 0; i < f.getPropertiesCount(); i += 2) {
            int key = f.getProperties(i);
            int val = f.getProperties(i+1);

            values.put(keys.get(key), decode(f.getValues(val)));
        }

        // id
        String id = f.getIdTypeCase() == IdTypeCase.INT_ID ? String.valueOf(f.getIntId()) : f.getId();

        return new BasicFeature(id, values);
    }

    Object decode(Value val) {
        ValueTypeCase t = val.getValueTypeCase();
        switch(t) {
            case STRING_VALUE:
                return val.getStringValue();
            case DOUBLE_VALUE:
                return val.getDoubleValue();
            case POS_INT_VALUE:
                return val.getPosIntValue();
            case NEG_INT_VALUE:
                return val.getNegIntValue();
            case BOOL_VALUE:
                return val.getBoolValue();
            case JSON_VALUE:
                return val.getJsonValue();
            case VALUETYPE_NOT_SET:
                return null;
            default:
                throw new UnsupportedOperationException("Unsupported value type: " + t);
        }
    }

    FeatureCursor decode(FeatureCollection fcol) {
        return new GeobufCursor(fcol, this);
    }

    CoordinateSequence readCoords(Geometry g, int start, int len, boolean close) {
        double[] coords = new double[dim*(len + (close?1:0))];

        long[] coord = new long[dim];
        for (int i = start; i < start+len; i++) {
            int j = i*dim;
            for (int k = 0; k < dim; k++) {
                coord[k] += g.getCoords(j+k);
                coords[(i-start)*dim+k] = coord[k] / e;
            }
        }

        if (close) {
            System.arraycopy(coords, 0, coords, coords.length-dim, dim);
        }

        return csFactory.create(coords, dim);
    }

    CoordinateSequence readAllCoords(Geometry g, boolean close) {
        return readCoords(g, 0, g.getCoordsCount()/dim, close);
    }

    @Override
    public void close() {
        try {
            in.close();
        } catch (IOException e) {
            LOG.debug("Error closing geobuf reader", e);
        }
    }
}
