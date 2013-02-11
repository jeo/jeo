package org.jeo.feature;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.vividsolutions.jts.geom.Geometry;

public class MapFeature implements Feature {

    public static MapFeature build(Object... kv) {
        LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
        return new MapFeature(map);
    }

    Schema schema;
    Map<String,Object> values;

    public MapFeature(Map<String,Object> values) {
        this(values, null);
    }

    public MapFeature(Map<String,Object> values, Schema schema) {
        this.values = values;
        this.schema = schema;
    }

    @Override
    public Schema schema() {
        if (schema == null) {
            schema = buildSchema();
        }
        return schema;
    }

    Schema buildSchema() {
        List<Field> fields = new ArrayList<Field>();
        for (Map.Entry<String, Object> e : values.entrySet()) {
            fields.add(new Field(e.getKey(), e.getValue() != null ? e.getValue().getClass() : null));
        }

        return new Schema("feature", fields);
    }

    @Override
    public Geometry geometry() {
        if (schema != null) {
            Field g = schema.geometry();
            return (Geometry) (g != null ? values.get(g.getName()) : null);
        }

        for (Object obj : values.values()) {
            if (obj instanceof Geometry) {
                return (Geometry) obj;
            }
        }

        return null;
    }

    @Override
    public Object get(String key) {
        return values.get(key);
    }

    @Override
    public void put(String key, Object val) {
        if (!values.containsKey(key)) {
            //new field, clear cached schema
            schema = null;
        }
        values.put(key, val);
    }

    @Override
    public List<Object> values() {
        List<Object> list = new ArrayList<Object>();
        for (Field f : schema()) {
            list.add(get(f.getName()));
        }
        return list;
    }
}
