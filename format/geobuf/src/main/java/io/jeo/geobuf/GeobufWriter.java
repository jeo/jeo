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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import io.jeo.data.Disposable;
import io.jeo.geobuf.Geobuf.Data;
import io.jeo.geobuf.Geobuf.Data.Feature;
import io.jeo.geobuf.Geobuf.Data.FeatureCollection;
import io.jeo.geobuf.Geobuf.Data.Geometry;
import io.jeo.geobuf.Geobuf.Data.Geometry.Type;
import io.jeo.geobuf.Geobuf.Data.Value;
import io.jeo.geom.Geom;
import io.jeo.geom.GeometryAdapter;
import io.jeo.proj.Proj;
import io.jeo.util.Function;
import io.jeo.vector.FeatureCursor;
import io.jeo.vector.Features;
import io.jeo.vector.Field;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import static io.jeo.geobuf.CustomKeys.CRS;

/**
 * Writes a geobuf protocol buffer stream.
 */
public class GeobufWriter implements Disposable {

    static Logger LOG = LoggerFactory.getLogger(GeobufWriter.class);

    OutputStream out;
    Data.Builder data;

    int dim = 2;
    double e = 1;
    double maxPrecision = 1E6;
    CoordinateReferenceSystem crs;

    Map<String,Integer> keys = new LinkedHashMap<>();
    int keyIndex;

    FeatureCollection.Builder fcb;
    Feature.Builder fb;
    Value.Builder vb;

    public GeobufWriter(OutputStream out) {
        this.out = out;
        data = Data.newBuilder();
        fcb = FeatureCollection.newBuilder();
        fb = Feature.newBuilder();
        vb = Value.newBuilder();
    }

    public GeobufWriter dimension(int dim) {
        this.dim = dim;
        return this;
    }

    public GeobufWriter maxPrecision(double maxPrecision) {
        this.maxPrecision = maxPrecision;
        return this;
    }

    public GeobufWriter write(Object obj) throws IOException {
        obj = analyze(obj);

        // add custom property keys
        keys.put(CRS, keyIndex++);

        // encode keys
        data.addAllKeys(keys.keySet());

        if (obj instanceof com.vividsolutions.jts.geom.Geometry) {
            encode((com.vividsolutions.jts.geom.Geometry)obj);
        }
        else if (obj instanceof io.jeo.vector.Feature) {
            encode((io.jeo.vector.Feature)obj);
        }
        else if (obj instanceof FeatureCursor) {
            encode((FeatureCursor)obj);
        }
        else {
            throw new IllegalArgumentException("Unable to encode object as geobuf: " + obj);
        }

        return write();
    }

    public GeobufWriter append(io.jeo.vector.Feature f) throws IOException {
        if (data.getFeatureCollection() == null) {
            data.setFeatureCollection(fcb);
        }

        fcb.addFeatures(doEncode(f));
        return this;
    }

    public GeobufWriter write() throws IOException {
        data.build().writeTo(out);
        return this;
    }

    <T> T analyze(T obj) throws IOException {
        if (obj instanceof com.vividsolutions.jts.geom.Geometry) {
            com.vividsolutions.jts.geom.Geometry g = (com.vividsolutions.jts.geom.Geometry) obj;
            Geom.visit(g, new GeometryAdapter(true) {
                @Override
                public void visit(Point point) {
                    upPrecision(point);
                    //dim = Math.max(dim, point.getCoordinateSequence().getDimension());

                }

                @Override
                public void visit(LineString line) {
                    upPrecision(line.getStartPoint()).upPrecision(line.getEndPoint());
                    //dim = Math.max(dim, line.getCoordinateSequence().getDimension());
                }
            });

            // adjust dimension and precision

            if (dim != 2) {
                data.setDimensions(dim);
            }

            int p = (int) Math.ceil(Math.log(e) / Math.log(10));
            if (p != 6) {
                data.setPrecision(p);
            }

        }
        else if (obj instanceof io.jeo.vector.Feature){
            io.jeo.vector.Feature f = (io.jeo.vector.Feature) obj;
            analyze(f.geometry());

            // calculate key / value index
            for (Map.Entry<String,Object> kv : f.map().entrySet()) {
                if (kv.getValue() instanceof com.vividsolutions.jts.geom.Geometry) {
                    continue;
                }

                String key = kv.getKey();
                if (!keys.containsKey(key)) {
                    keys.put(key, keyIndex++);
                }
            }

            // crs
            if (crs == null) {
                crs = Features.crs(f);
            }
        }
        else if (obj instanceof FeatureCursor) {
            // rather than scan through entire cursor, do the first n features
            // TODO: make n configurable
            int n = 10;
            FeatureCursor cursor = ((FeatureCursor) obj).buffer(n);
            for (int i = 0; i < n && cursor.hasNext(); i++) {
                analyze(cursor.next());
            }

            cursor.rewind();
            return (T) cursor;
        }

        return obj;
    }

    GeobufWriter upPrecision(Point point) {
        upPrecision(point.getX()).upPrecision(point.getY());
        if (dim > 2) {
            upPrecision(point.getCoordinate().z);
        }
        return this;
    }

    GeobufWriter upPrecision(double val) {
        while (Math.round(val * e) / e != val && e < maxPrecision) e *= 10;
        return this;
    }

    GeobufWriter encode(com.vividsolutions.jts.geom.Geometry g) throws IOException {
        data.setGeometry(doEncode(g));
        return this;
    }

    Geometry doEncode(com.vividsolutions.jts.geom.Geometry g) {
        if (g == null) {
            return null;
        }

        Geometry.Builder b = Geometry.newBuilder();

        switch(Geom.Type.from(g)) {
            case POINT:
                encode((Point) g, b);
                break;
            case LINESTRING:
                encode((LineString) g, b);
                break;
            case POLYGON:
                encode((Polygon) g, b);
                break;
            case MULTIPOINT:
                encode((MultiPoint) g, b);
                break;
            case MULTILINESTRING:
                encode((MultiLineString) g, b);
                break;
            case MULTIPOLYGON:
                encode((MultiPolygon) g, b);
                break;
            default:
                throw new IllegalArgumentException("Unable to encode geometry " + g);
        }

        return b.build();
    }

    GeobufWriter encode(Point p, Geometry.Builder b) {
        b.setType(Type.POINT);
        return coord(p.getCoordinate(), b);
    }

    GeobufWriter encode(LineString l, Geometry.Builder b) {
        b.setType(Type.LINESTRING);
        return coords(l, b);
    }

    GeobufWriter encode(Polygon p, Geometry.Builder b) {
        b.setType(Type.POLYGON);
        return coords(p, b, false);
    }

    GeobufWriter encode(final MultiPoint mp, Geometry.Builder b) {
        b.setType(Type.MULTIPOINT);
        b.addAllCoords(new Iterable<Long>() {
            @Override
            public Iterator<Long> iterator() {
                return new CoordIterator(new CoordinateArraySequence(mp.getCoordinates()), false);
            }
        });
        return this;
    }

    GeobufWriter encode(final MultiLineString ml, Geometry.Builder b) {
        b.setType(Type.MULTILINESTRING);
        if (ml.getNumGeometries() > 1) {
            for (LineString line : Geom.iterate(ml)) {
                b.addLengths(line.getNumPoints());
                coords(line, b);
            }
        }
        else {
            coords((LineString) ml.getGeometryN(0), b);
        }
        return this;
    }

    GeobufWriter encode(final MultiPolygon mp, Geometry.Builder b) {
        b.setType(Type.MULTIPOLYGON);
        if (mp.getNumGeometries() != 1 || Geom.first(mp).getNumInteriorRing() > 0) {
            // encode with lengths
            b.addLengths(mp.getNumGeometries());
            for (Polygon p : Geom.iterate(mp)) {
                b.addLengths(1+p.getNumInteriorRing());
                coords(p, b, true);
            }
        }
        else {
            coords(Geom.first(mp).getExteriorRing(), b);
        }

        return this;
    }

    GeobufWriter encode(io.jeo.vector.Feature f) {
        data.setFeature(doEncode(f));
        return this;
    }

    Feature doEncode(io.jeo.vector.Feature f) {
        fb.clear();

        // geometry
        fb.setGeometry(doEncode(f.geometry()));

        // values
        int i = 0;
        for (Map.Entry<String,Object> kv : f.map().entrySet()) {
            Object val = kv.getValue();
            if (val == null || val instanceof com.vividsolutions.jts.geom.Geometry) {
                continue;
            }
            fb.addValues(encodeValue(val));
            fb.addProperties(keys.get(kv.getKey()));
            fb.addProperties(i++);
        }

        return fb.build();
    }

    GeobufWriter encode(FeatureCursor cursor) throws IOException {
        FeatureCollection.Builder b = FeatureCollection.newBuilder();

        if (crs != null) {
            b.addValues(encodeValue(Proj.toString(crs)));
            b.addCustomProperties(keys.get(CRS));
            b.addCustomProperties(b.getValuesCount()-1);
        }

        b.addAllFeatures(cursor.map(new Function<io.jeo.vector.Feature, Feature>() {
            @Override
            public Feature apply(io.jeo.vector.Feature f) {
                return doEncode(f);
            }
        }));
        data.setFeatureCollection(b.build());
        return this;
    }

    Value encodeValue(Object obj) {
        vb.clear();

        if (obj instanceof Boolean) {
            vb.setBoolValue(((Boolean) obj).booleanValue());
        }
        else if (obj instanceof Number) {
            Number n = (Number) obj;
            if (n.doubleValue() % 1 != 0) {
                vb.setDoubleValue(n.doubleValue());
            }
            else {
                long l = n.longValue();
                if (l < 0) {
                    vb.setNegIntValue(-l);
                }
                else {
                    vb.setPosIntValue(l);
                }
            }
        }
        else {
            vb.setStringValue(obj.toString());
        }

        return vb.build();
    }

    GeobufWriter coord(double val, Geometry.Builder b) {
        b.addCoords(Math.round(val * e));
        return this;
    }

    GeobufWriter coord(Coordinate coord, Geometry.Builder b) {
        coord(coord.x, b).coord(coord.y, b);
        if (dim > 2) {
            coord(coord.z, b);
        }
        return this;
    }

    GeobufWriter coords(final LineString line, Geometry.Builder b) {
        b.addAllCoords(new Iterable<Long>() {
            @Override
            public Iterator<Long> iterator() {
                return new CoordIterator(line.getCoordinateSequence(), line instanceof LinearRing);
            }
        });
        return this;
    }

    GeobufWriter coords(Polygon p, Geometry.Builder b, boolean lengths) {
        if (lengths || p.getNumInteriorRing() > 0) {
            b.addLengths(p.getExteriorRing().getNumPoints()-1);
            for (LineString hole : Geom.holes(p)) {
                b.addLengths(hole.getNumPoints()-1);
            }
        }

        coords(p.getExteriorRing(), b);
        if (p.getNumInteriorRing() > 0) {
            for (LineString hole : Geom.holes(p)) {
                coords(hole, b);
            }
        }

        return this;
    }

    class CoordIterator implements Iterator<Long> {

        CoordinateSequence seq;
        int n;

        int c = 0; // coordinate index
        int o = 0; // ordinate index
        double[] last = new double[dim];

        public CoordIterator(CoordinateSequence seq, boolean close) {
            this.seq = seq;
            n = seq.size();
            if (close) {
                n--;
            }
        }

        @Override
        public boolean hasNext() {
            return c < n;
        }

        @Override
        public Long next() {
            double val = seq.getOrdinate(c, o);
            long l = Math.round((val-last[o]) * e);
            last[o] = val;
            if (++o >= dim) {
                c++;
                o = 0;
            }
            return l;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }


    public void close() {
        try {
            out.flush();
            out.close();
        }
        catch(IOException e) {
            LOG.debug("Error closing geobuf writer", e);
        }
    }
}
