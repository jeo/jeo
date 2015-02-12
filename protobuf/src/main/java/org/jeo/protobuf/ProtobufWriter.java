package org.jeo.protobuf;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import org.jeo.geom.Geom;
import org.jeo.proj.Proj;
import org.jeo.protobuf.Feat.Feature;
import org.jeo.protobuf.Feat.Field;
import org.jeo.protobuf.Feat.Schema;
import org.jeo.protobuf.Feat.Value;
import org.jeo.protobuf.Geom.Array;
import org.jeo.protobuf.Geom.Geometry;
import org.jeo.protobuf.Geom.Geometry.Type;
import org.jeo.protobuf.Geom.LineString;
import org.jeo.protobuf.Geom.MultiLineString;
import org.jeo.protobuf.Geom.MultiPoint;
import org.jeo.protobuf.Geom.MultiPolygon;
import org.jeo.protobuf.Geom.Point;
import org.jeo.protobuf.Geom.Polygon;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.google.protobuf.ByteString;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;

public class ProtobufWriter {

    OutputStream out;

    public static Point encode(com.vividsolutions.jts.geom.Point p) {
        return Point.newBuilder().setX(p.getX()).setY(p.getY()).build();
    }

    public static LineString encode(com.vividsolutions.jts.geom.LineString l) {
        return LineString.newBuilder().setCoords(array(l.getCoordinateSequence())).build();
    }

    public static Polygon encode(com.vividsolutions.jts.geom.Polygon p) {
        Polygon.Builder b = Polygon.newBuilder();

        b.setRing(array(p.getExteriorRing().getCoordinateSequence()));
        for (int i = 0; i < p.getNumInteriorRing(); i++) {
            b.addHoles(array(p.getInteriorRingN(i).getCoordinateSequence()));
        }

        return b.build();
    }

    public static MultiPoint encode(com.vividsolutions.jts.geom.MultiPoint mp) {
        Array.Builder b = Array.newBuilder().setDim(2);
        for (Coordinate c : mp.getCoordinates()) {
            b.addOrd(c.x);
            b.addOrd(c.y);
        }

        return MultiPoint.newBuilder().setMembers(b.build()).build();
    }

    public static MultiLineString encode(com.vividsolutions.jts.geom.MultiLineString ml) {
        MultiLineString.Builder b = MultiLineString.newBuilder();
        for (int i = 0; i < ml.getNumGeometries(); i++) {
            b.addMembers(array((
                (com.vividsolutions.jts.geom.LineString)ml.getGeometryN(i)).getCoordinateSequence()));
        }

        return b.build();
    }

    public static MultiPolygon encode(com.vividsolutions.jts.geom.MultiPolygon mp) {
        MultiPolygon.Builder b = MultiPolygon.newBuilder();

        for (int i = 0; i < mp.getNumGeometries(); i++) {
            b.addMembers(encode((com.vividsolutions.jts.geom.Polygon)mp.getGeometryN(i)));
        }

        return b.build();
    }

    public Geometry encode(com.vividsolutions.jts.geom.Geometry g) throws IOException {
        Geometry.Builder b = Geometry.newBuilder();

        switch(Geom.Type.from(g)) {
        case POINT:
            b.setType(Type.POINT);
            b.setPoint(encode((com.vividsolutions.jts.geom.Point) g));
            break;
        case LINESTRING:
            b.setType(Type.LINESTRING);
            b.setLineString(encode((com.vividsolutions.jts.geom.LineString) g));
            break;
        case POLYGON:
            b.setType(Type.POLYGON);
            b.setPolygon(encode((com.vividsolutions.jts.geom.Polygon) g));
            break;
        case MULTIPOINT:
            b.setType(Type.MULTIPOINT);
            b.setMultiPoint(encode((com.vividsolutions.jts.geom.MultiPoint) g));
            break;
        case MULTILINESTRING:
            b.setType(Type.MULTILINESTRING);
            b.setMultiLineString(encode((com.vividsolutions.jts.geom.MultiLineString) g));
            break;
        case MULTIPOLYGON:
            b.setType(Type.MULTIPOLYGON);
            b.setMultiPolygon(encode((com.vividsolutions.jts.geom.MultiPolygon) g));
            break;
        default:
            throw new IllegalArgumentException("Unable to encode geometry " + g);
        }

        return b.build();
    }

    static Array array(CoordinateSequence seq) {
        Array.Builder b = Array.newBuilder();

        int dim = seq.getDimension();
        b.setDim(dim);

        for (int i = 0; i < seq.size(); i++) {
            for (int j = 0; j < dim; j++) {
                b.addOrd(seq.getOrdinate(i, j));
            }
        }

        return b.build();
    }

    public ProtobufWriter(OutputStream out) {
        this.out = out;
    }

    public ProtobufWriter point(com.vividsolutions.jts.geom.Point p) throws IOException {
        encode(p).writeDelimitedTo(out);
        return this;
    }

    public ProtobufWriter lineString(com.vividsolutions.jts.geom.LineString l) throws IOException {
        encode(l).writeDelimitedTo(out);
        return this;
    }

    public ProtobufWriter polygon(com.vividsolutions.jts.geom.Polygon p) throws IOException {
        encode(p).writeDelimitedTo(out);
        return this;
    }

    public ProtobufWriter multiPoint(com.vividsolutions.jts.geom.MultiPoint mp) throws IOException {
        encode(mp).writeDelimitedTo(out);
        return this;
    }

    public ProtobufWriter multiLineString(com.vividsolutions.jts.geom.MultiLineString ml) 
        throws IOException {

        encode(ml).writeDelimitedTo(out);
        return this;
    }

    public ProtobufWriter multiPolygon(com.vividsolutions.jts.geom.MultiPolygon mp) 
        throws IOException {
        encode(mp).writeDelimitedTo(out);
        return this;
    }

    public ProtobufWriter geometry(com.vividsolutions.jts.geom.Geometry g) throws IOException {
        switch(Geom.Type.from(g)) {
        case POINT:
            return point((com.vividsolutions.jts.geom.Point) g);
        case LINESTRING:
            return lineString((com.vividsolutions.jts.geom.LineString) g);
        case POLYGON:
            return polygon((com.vividsolutions.jts.geom.Polygon) g);
        case MULTIPOINT:
            return multiPoint((com.vividsolutions.jts.geom.MultiPoint) g);
        case MULTILINESTRING:
            return multiLineString((com.vividsolutions.jts.geom.MultiLineString) g);
        case MULTIPOLYGON:
            return multiPolygon((com.vividsolutions.jts.geom.MultiPolygon) g);
        default:
            throw new IllegalArgumentException("Unable to encode geometry " + g);
        }
    }

    public ProtobufWriter feature(org.jeo.vector.Feature f) throws IOException {
        Feature.Builder b = Feature.newBuilder();

        com.vividsolutions.jts.geom.Geometry g = f.geometry();
        if (g != null) {
            b.setGeom(encode(g));
        }

        for (Map.Entry<String,Object> e : f.map().entrySet()) {
            if (e.getKey() == null || e.getValue() == null) {
                continue;
            }
            if (e.getValue() instanceof com.vividsolutions.jts.geom.Geometry && e.getValue() == g) {
                continue;
            }

            Object val = e.getValue();

            Value.Builder vb = Value.newBuilder();
            if (val instanceof Integer) {
                vb.setIntVal((Integer) val);
            }
            else if (val instanceof Double) {
                vb.setDoubleVal((Double)val);
            }
            else if (val instanceof byte[]) {
                vb.setBytesVal(ByteString.copyFrom((byte[])val));
            }
            else {
                vb.setStrVal(val.toString());
            }
            b.addValue(vb.build());
        }

        b.build().writeDelimitedTo(out);
        return this;
    }

    public ProtobufWriter schema(org.jeo.vector.Schema schema) throws IOException {
        Schema.Builder b = Schema.newBuilder();
        b.setName(schema.getName());

        org.jeo.vector.Field geom = schema.geometry();
        if (geom != null) {
            Type gtype = null;
            switch(Geom.Type.from(geom.getType())) {
            case POINT: gtype = Type.POINT; break;
            case LINESTRING: gtype = Type.LINESTRING; break;
            case POLYGON: gtype = Type.POLYGON; break;
            case MULTIPOINT: gtype = Type.MULTIPOINT; break;
            case MULTILINESTRING: gtype = Type.MULTILINESTRING; break;
            case MULTIPOLYGON: gtype = Type.MULTIPOLYGON; break;
            case GEOMETRYCOLLECTION: gtype = Type.GEOMETRYCOLLECTION; break;
            case GEOMETRY: gtype = Type.GEOMETRY;
            }

            b.setGeomType(gtype);

            CoordinateReferenceSystem crs = geom.getCRS();
            if (crs != null) {
                b.setCrs(Proj.toString(crs));
            }
        }
        
        for (org.jeo.vector.Field fld : schema) {
            if (fld.getName().equals(geom.getName())) {
                continue;
            }

            Field.Builder fb = Field.newBuilder().setKey(fld.getName());

            Class<?> clazz = fld.getType();
            if (clazz == Integer.class) {
                fb.setType(Field.Type.INT);
            }
            else if (clazz == Double.class) {
                fb.setType(Field.Type.DOUBLE);
            }
            else if (clazz == byte[].class) {
                fb.setType(Field.Type.BINARY);
            }
            else {
                fb.setType(Field.Type.STRING);
            }

            b.addField(fb.build());
        }

        b.build().writeDelimitedTo(out);
        return this;
    }

    public void close() throws IOException {
        out.flush();
        out.close();
    }
}
