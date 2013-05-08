package org.jeo.mongo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.jeo.feature.AbstractFeature;
import org.jeo.feature.Schema;
import org.jeo.feature.SchemaBuilder;
import org.jeo.geom.Geom;
import org.jeo.proj.Proj;

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

public class GeoJSONFeature extends AbstractFeature {

    DBObject obj;
    String dbcolName;

    GeoJSONFeature(DBObject dbobj, String dbcolName) {
        super(id(dbobj));
        this.obj = dbobj;
        this.dbcolName = dbcolName;
    }

    static String id(DBObject obj) {
        ObjectId id = (ObjectId) obj.get("_id");
        id = id != null ? id : ObjectId.get();
        return id.toString();
    }

    @Override
    public Object get(String key) {
        if ("geometry".equals(key)) {
            return toGeometry((DBObject)find("geometry"));
        }
        else {
            return find("properties", key);
        }
    }

    @Override
    public void put(String key, Object val) {
        if ("geometry".equals(key)) {
            if (val instanceof Geometry){
                Geometry geo = (Geometry) val;
                set(toObject(geo), "geometry");
            }
            else {
                throw new IllegalArgumentException("Value " + val + " is not a geometry");
            }
        }
        else {
            set(val, "properties", key);
        }
    }

    @Override
    protected Geometry findGeometry() {
        return (Geometry) get("geometry");
    }

    @Override
    protected Schema buildSchema() {
        SchemaBuilder sb = new SchemaBuilder(dbcolName);
        DBObject geo = (DBObject) find("geometry");
        if (geo != null) {
            Geom.Type type = Geom.Type.from((String)geo.get("type"));
            sb.field("geometry", type != null ? type.getType() : Geometry.class, Proj.EPSG_4326); 
        }
        else {
            sb.field("geometry", Geometry.class, Proj.EPSG_4326);
        }

        DBObject props = (DBObject) get("properties");
        if (props != null) {
            for (String key : props.keySet()) {
                Object val = props.get(key);
                sb.field(key, val != null ? val.getClass() : Object.class);
            }
        }

        return sb.schema();
    }

    @Override
    public List<Object> list() {
        List<Object> list = new ArrayList<Object>();

        list.add(get("geometry"));

        DBObject props = (DBObject) get("properties");
        if (props != null) {
            for (String key : props.keySet()) {
                list.add(props.get(key));
            }
        }

        return list;
    }

    @Override
    public Map<String, Object> map() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();

        map.put("geometry", get("geometry"));

        DBObject props = (DBObject) get("properties");
        if (props != null) {
            for (String key : props.keySet()) {
                map.put(key, props.get(key));
            }
        }

        return map;
    }

    void set(Object val, String... path) {
        DBObject obj = this.obj;
        for (int i = 0; i < path.length-1; i++) {
            String part = path[i];
            Object next = obj.get(part);
            if (next == null) {
                next = new BasicDBObject();
                obj.put(part, next);
            }

            if (!(next instanceof DBObject)) {
                throw new IllegalArgumentException("Illegal path, " + part + " is not an object");
            }
            obj = (DBObject)next; 
        }

        obj.put(path[path.length-1], val);
    }

    Object find(String... path) {
        DBObject obj = this.obj;
        Object next = obj;
        for (int i = 0; i < path.length; i++) {
            String part = path[i];
            next = obj.get(part);
            if (next == null) {
                return null;
            }

            if (i < path.length-1) {
                if (!(next instanceof DBObject)) {
                    throw new IllegalArgumentException("Illegal path, " + part + " is not an object");
                }
                obj = (DBObject) next;
            }
        }
        return next;
    }

    DBObject toObject(Geometry g) {
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

    Geometry toGeometry(DBObject obj) {
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

    DBObject toObject(Point p) {
        BasicDBList coords = new BasicDBList();
        coords.add(p.getX());
        coords.add(p.getY());

        BasicDBObject obj = new BasicDBObject("type", "Point");
        obj.put("coordinates", coords);
        return obj;
    }

    Point toPoint(DBObject obj) {
        BasicDBList list = (BasicDBList) obj.get("coordinates");
        return new GeometryFactory().createPoint(toCoordinate(list));
    }

    DBObject toObject(LineString l) {
        BasicDBObject obj = new BasicDBObject("type", "LineString");
        obj.put("coordinates", toList(l.getCoordinateSequence()));
        return obj;
    }

    LineString toLineString(DBObject obj) {
        BasicDBList list = (BasicDBList) obj.get("coordinates");
        return new GeometryFactory().createLineString(toCoordinates(list));
    }

    DBObject toObject(Polygon p) {
        BasicDBList coords = new BasicDBList();
        coords.add(toList(p.getExteriorRing().getCoordinateSequence()));
        for (int i = 0; i < p.getNumInteriorRing(); i++) {
            coords.add(toList(p.getInteriorRingN(i).getCoordinateSequence()));
        }
        BasicDBObject obj = new BasicDBObject("type", "Polygon");
        obj.put("coordinates", coords);
        return obj;
    }

    Polygon toPolygon(DBObject obj) {
        BasicDBList list = (BasicDBList) obj.get("coordinates");
        return toPolygon(list);
    }

    DBObject toObject(MultiPoint mp) {
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

    MultiPoint toMultiPoint(DBObject obj) {
        BasicDBList list = (BasicDBList) obj.get("coordinates");
        return new GeometryFactory().createMultiPoint(toCoordinates(list));
    }

    DBObject toObject(MultiLineString ml) {
        BasicDBObject obj = new BasicDBObject("type", "MultiLineString");
        BasicDBList coords = new BasicDBList();
        for (LineString l : Geom.iterate(ml)) {
            coords.add(toList(l.getCoordinateSequence()));
        }
        obj.put("coordinates", coords);
        return obj;
    }

    MultiLineString toMultiLineString(DBObject obj) {
        GeometryFactory gf = new GeometryFactory();

        BasicDBList list = (BasicDBList) obj.get("coordinates");
        LineString[] lines = new LineString[list.size()];

        for (int i = 0; i < list.size(); i++) {
            lines[i] = gf.createLineString(toCoordinates((BasicDBList)list.get(i)));
        }

        return gf.createMultiLineString(lines);
    }

    DBObject toObject(MultiPolygon mp) {
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

    MultiPolygon toMultiPolygon(DBObject obj) {
        GeometryFactory gf = new GeometryFactory();

        BasicDBList list = (BasicDBList) obj.get("coordinates");
        Polygon[] polys = new Polygon[list.size()];
        
        for (int i = 0; i < list.size(); i++) {
            polys[i] = toPolygon((BasicDBList)list.get(i));
        }

        return gf.createMultiPolygon(polys);
    }

    DBObject toObject(GeometryCollection gc) {
        BasicDBObject obj = new BasicDBObject("type", "GeometryCollection");
        BasicDBList geoms = new BasicDBList();

        for (Geometry g : Geom.iterate(gc)) {
            geoms.add(toObject(g));
        }
        return obj;
    }

    GeometryCollection toCollection(DBObject obj) {
        BasicDBList list = (BasicDBList) obj.get("geometries");

        Geometry[] geoms = new Geometry[list.size()];

        for (int i = 0; i < list.size(); i++) {
            geoms[i] = toGeometry((DBObject) list.get(i));
        }

        return new GeometryFactory().createGeometryCollection(geoms);
    }

    BasicDBList toList(CoordinateSequence coordseq) {
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

    Coordinate toCoordinate(BasicDBList list) {
        return new Coordinate(((Number)list.get(0)).doubleValue(), ((Number)list.get(1)).doubleValue());
    }

    Coordinate[] toCoordinates(BasicDBList list) {
        Coordinate[] coords = new Coordinate[list.size()];
        for (int i = 0; i < list.size(); i++) {
            coords[i] = toCoordinate( (BasicDBList) list.get(i));
        }
        return coords;
    }

    Polygon toPolygon(BasicDBList list) {
        GeometryFactory gf = new GeometryFactory();

        LinearRing shell = gf.createLinearRing(toCoordinates((BasicDBList) list.get(0)));

        LinearRing[] holes = list.size() > 1 ? new LinearRing[list.size()-1] : null;
        for (int i = 1; i < list.size(); i++) {
            holes[i-1] = gf.createLinearRing(toCoordinates((BasicDBList) list.get(i)));
        }

        return new GeometryFactory().createPolygon(shell, holes);
    }
}
