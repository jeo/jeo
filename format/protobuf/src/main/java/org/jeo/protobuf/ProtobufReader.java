
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
 */package org.jeo.protobuf;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.jeo.protobuf.Feat.Field.Type;
import org.jeo.vector.BasicFeature;
import org.jeo.vector.SchemaBuilder;
import org.jeo.geom.Geom;
import org.jeo.proj.Proj;
import org.jeo.protobuf.Feat.Feature;
import org.jeo.protobuf.Feat.Field;
import org.jeo.protobuf.Feat.Schema;
import org.jeo.protobuf.Feat.Value;
import org.jeo.protobuf.Geom.Array;
import org.jeo.protobuf.Geom.Geometry;
import org.jeo.protobuf.Geom.LineString;
import org.jeo.protobuf.Geom.MultiLineString;
import org.jeo.protobuf.Geom.MultiPoint;
import org.jeo.protobuf.Geom.MultiPolygon;
import org.jeo.protobuf.Geom.Point;
import org.jeo.protobuf.Geom.Polygon;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;

/**
 * Reads a protocol buffer stream.
 */
public class ProtobufReader {

    static GeometryFactory gf = new GeometryFactory();

    int id = 0;
    InputStream in;
    Feature.Builder last;
    End end;

    public static com.vividsolutions.jts.geom.Point decode(Point p) {
        return Geom.point(p.getX(), p.getY());
    }

    public static com.vividsolutions.jts.geom.LineString decode(LineString l) {
        return gf.createLineString(cs(l.getCoords()));
    }

    public static com.vividsolutions.jts.geom.Polygon decode(Polygon p) {
        LinearRing shell = new LinearRing(cs(p.getRing()), gf);
        LinearRing[] holes = new LinearRing[p.getHolesCount()];
        for (int i = 0; i < p.getHolesCount(); i++) {
            holes[i] = new LinearRing(cs(p.getHoles(i)), gf);
        }

        return gf.createPolygon(shell, holes);
    }

    public static com.vividsolutions.jts.geom.MultiPoint decode(MultiPoint mp) {
        return gf.createMultiPoint(cs(mp.getMembers()));
    }

    public static com.vividsolutions.jts.geom.MultiLineString decode(MultiLineString ml) {
        com.vividsolutions.jts.geom.LineString[] lines = 
            new com.vividsolutions.jts.geom.LineString[ml.getMembersCount()];
        for (int i = 0; i < ml.getMembersCount(); i++) {
            lines[i] = gf.createLineString(cs(ml.getMembers(i)));
        }

        return gf.createMultiLineString(lines);
    }

    public static com.vividsolutions.jts.geom.MultiPolygon decode(MultiPolygon mp) {
        com.vividsolutions.jts.geom.Polygon[] polys = 
            new com.vividsolutions.jts.geom.Polygon[mp.getMembersCount()];
       
        for (int i = 0; i < mp.getMembersCount(); i++) {
            polys[i] = decode(mp.getMembers(i));
        }

        return gf.createMultiPolygon(polys);
    }

    public static com.vividsolutions.jts.geom.Geometry decode(Geometry g) {
        switch(g.getType()) {
        case POINT:
            return decode(g.getPoint());
        case LINESTRING:
            return decode(g.getLineString());
        case POLYGON:
            return decode(g.getPolygon());
        case MULTIPOINT:
            return decode(g.getMultiPoint());
        case MULTILINESTRING:
            return decode(g.getMultiLineString());
        case MULTIPOLYGON:
            return decode(g.getMultiPolygon());
        }
        throw new IllegalArgumentException("Not supported: " + g);
    }

    public ProtobufReader(InputStream in) {
        this.in = in;
        end = new End() {
            @Override
            public boolean isEnd() throws IOException {
                return ProtobufReader.this.in.available() == 0;
            }
        };
    }

    /**
     * Forces the reader to read until the last feature in the stream.
     * <p>
     * By default the reader will read until no more bytes are available in the input stream.
     * When this flag is set the reader will read until the feature marked as last in the stream
     * has been encountered.
     * </p>
     */
    public ProtobufReader setReadUntilLastFeature() {
        end = new End() {
            @Override
            public boolean isEnd() throws IOException {
                return last != null && last.getLast();
            }
        };
        return this;
    }

    public com.vividsolutions.jts.geom.Point point() throws IOException {
        if (eoi()) {
            return null;
        }
        Point.Builder builder = Point.newBuilder();
        builder.mergeDelimitedFrom(in);

        Point p = builder.build();
        return decode(p);
    }

    public com.vividsolutions.jts.geom.LineString lineString() throws IOException {
        if (eoi()) {
            return null;
        }
        LineString.Builder builder = LineString.newBuilder();
        builder.mergeDelimitedFrom(in);

        LineString l = builder.build();
        return decode(l);
    }

    public com.vividsolutions.jts.geom.Polygon polygon() throws IOException {
        if (eoi()) {
            return null;
        }
        Polygon.Builder builder = Polygon.newBuilder();
        builder.mergeDelimitedFrom(in);

        Polygon p = builder.build();
        return decode(p);
    }

    public com.vividsolutions.jts.geom.MultiPoint multiPoint() throws IOException {
        if (eoi()) {
            return null;
        }
        MultiPoint.Builder builder = MultiPoint.newBuilder();
        builder.mergeDelimitedFrom(in);

        MultiPoint mp = builder.build();

        return decode(mp);
    }

    public com.vividsolutions.jts.geom.MultiLineString multiLineString() throws IOException {
        if (eoi()) {
            return null;
        }
        MultiLineString.Builder builder = MultiLineString.newBuilder();
        builder.mergeDelimitedFrom(in);

        MultiLineString ml = builder.build();
        return decode(ml);
    }

    public com.vividsolutions.jts.geom.MultiPolygon multiPolygon() throws IOException {
        if (eoi()) {
            return null;
        }

        MultiPolygon.Builder builder = MultiPolygon.newBuilder();
        builder.mergeDelimitedFrom(in);

        MultiPolygon mp = builder.build();
        return decode(mp);
    }

    public org.jeo.vector.Feature feature(org.jeo.vector.Schema schema) throws IOException {
        if (eoi()) {
            return null;
        }

        Feature.Builder b = Feature.newBuilder();
        b.mergeDelimitedFrom(in);

        List<Object> vals = new ArrayList<Object>();

        for (int i = 0; i < b.getValueCount(); i++) {
            Value val = b.getValue(i);
            if (val.hasIntVal()) {
                vals.add(val.getIntVal());
            }
            else if (val.hasDoubleVal()) {
                vals.add(val.getDoubleVal());
            }
            else if (val.hasStrVal()) {
                vals.add(val.getStrVal());
            }
            else if (val.hasBytesVal()) {
                vals.add(val.getBytesVal().toByteArray());
            }
            else if (val.hasGeom()) {
                vals.add(decode(val.getGeom()));
            }
            else {
                vals.add(null);
            }
        }

        last = b;
        return new BasicFeature(String.valueOf(id++), vals, schema);
    }

    public org.jeo.vector.Schema schema() throws IOException {
        if (eoi()) {
            return null;
        }

        Schema.Builder b = Schema.newBuilder();
        b.mergeDelimitedFrom(in);

        Schema s = b.build();

        SchemaBuilder sb = org.jeo.vector.Schema.build(s.getName());

        for (int i = 0; i < b.getFieldCount(); i++) {
            Field fld = b.getField(i);
            String key = fld.getKey();
            Class<?> clazz = null;
            CoordinateReferenceSystem crs = null;

            switch(fld.getType()) {
            case INT: clazz = Integer.class; break;
            case DOUBLE: clazz = Double.class; break;
            case STRING: clazz = String.class; break;
            case BINARY: clazz = byte[].class; break;
            case GEOMETRY:
                switch(fld.getGeomType()) {
                    case POINT:
                        clazz = com.vividsolutions.jts.geom.Point.class; break;
                    case LINESTRING:
                        clazz = com.vividsolutions.jts.geom.LineString.class; break;
                    case POLYGON:
                        clazz = com.vividsolutions.jts.geom.Polygon.class; break;
                    case MULTIPOINT:
                        clazz = com.vividsolutions.jts.geom.MultiPoint.class; break;
                    case MULTILINESTRING:
                        clazz = com.vividsolutions.jts.geom.MultiLineString.class; break;
                    case MULTIPOLYGON:
                        clazz = com.vividsolutions.jts.geom.MultiPolygon.class; break;
                    case GEOMETRYCOLLECTION:
                        clazz = com.vividsolutions.jts.geom.GeometryCollection.class; break;
                    default:
                        clazz = com.vividsolutions.jts.geom.Geometry.class;
                }
                if (fld.hasCrs()) {
                    crs = Proj.crs(fld.getCrs());
                }
                break;
            default: clazz = Object.class;
            }

            if (fld.getType() == Type.GEOMETRY) {
                sb.field(key, (Class<? extends com.vividsolutions.jts.geom.Geometry>) clazz, crs);
            }
            else {
                sb.field(key, clazz);
            }
        }

        return sb.schema();
    }

    public void close() throws IOException {
        in.close();
    }

    static CoordinateSequence cs(Array array) {
        return new PBCoordinateSequence(array);
    }

    static interface End {
        boolean isEnd() throws IOException;
    }

    boolean eoi() throws IOException {
        return end.isEnd();
    }
}
