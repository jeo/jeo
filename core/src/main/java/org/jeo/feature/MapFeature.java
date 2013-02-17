package org.jeo.feature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.vividsolutions.jts.geom.Geometry;

public class MapFeature extends Feature {

    public static MapFeature build(Object... kv) {
        LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
        return new MapFeature(map);
    }

    Map<String,Object> values;

    public MapFeature(Map<String,Object> values) {
        this(values, null);
    }

    public MapFeature(Map<String,Object> values, Schema schema) {
        super(schema);
        this.values = values;
    }

    protected Schema buildSchema() {
        List<Field> fields = new ArrayList<Field>();
        for (Map.Entry<String, Object> e : values.entrySet()) {
            fields.add(new Field(e.getKey(), e.getValue() != null ? e.getValue().getClass() : null));
        }

        return new Schema("feature", fields);
    }

    @Override
    protected Geometry findGeometry() {
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
    public List<Object> list() {
        List<Object> list = new ArrayList<Object>();
        for (Field f : schema()) {
            list.add(get(f.getName()));
        }
        return list;
    }

    @Override
    public Map<String,Object> map() {
        return Collections.unmodifiableMap(values);
    }
}
