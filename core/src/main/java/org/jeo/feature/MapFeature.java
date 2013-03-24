package org.jeo.feature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.vividsolutions.jts.geom.Geometry;

public class MapFeature extends AbstractFeature {

    /**
     * Creates a feature object from a map.
     */
    public static MapFeature create(Map<String, Object> map) {
        return new MapFeature(null, map);
    }

    public static MapFeature create(Object... kv) {
        if (kv.length % 2 != 0) {
            throw new IllegalArgumentException("Odd number of argumets");
        }
        LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
        for (int i = 0; i < kv.length; i+=2) {
            map.put(kv[i].toString(), kv[i+1]);
        }
        return new MapFeature(null, map);
    }

    Map<String,Object> values;

    public MapFeature(String id, Map<String,Object> values) {
        this(id, values, null);
    }

    public MapFeature(String id, Map<String,Object> values, Schema schema) {
        super(id, schema);
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
