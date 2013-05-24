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
            return GeoJSON.toGeometry((DBObject)find("geometry"));
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
                set(GeoJSON.toObject(geo), "geometry");
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

        DBObject props = (DBObject) find("properties");
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

    
}
