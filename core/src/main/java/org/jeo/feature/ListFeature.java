package org.jeo.feature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Feature implementation backed by a List.
 * <p>
 * </p>
 * @author Justin Deoliveira, OpenGeo
 */
public class ListFeature extends AbstractFeature {

    List<Object> values;

    public ListFeature(String id, List<Object> values) {
        this(id, values, null);
    }

    public ListFeature(String id, List<Object> values, Schema schema) {
        super(id, schema);
        this.values = pad(values, schema);
    }

    List<Object> pad(List<Object> values, Schema schema) {
        //copy list passed in
        values = values != null ? new ArrayList<Object>(values) : new ArrayList<Object>();

        //expand up to size of schema if necessary
        if (schema != null) {
            while(values.size() < schema.size()) {
                values.add(null);
            }
        }
        return values;
    }

    public Object get(int i) {
        return values.get(i);
    }

    public void set(int i, Object value) {
        values.set(i,  value);
    }

    @Override
    public Object get(String key) {
        int i = schema().indexOf(key);
        return i != -1 ? get(i) : null;
    }

    @Override
    public void put(String key, Object val) {
        int i = schema().indexOf(key);
        if (i == -1) {
            throw new IllegalArgumentException("No such key " + key);
        }
        set(i, val);
    }

    @Override
    protected Geometry findGeometry() {
        for (Object o : values) {
            if (o instanceof Geometry) {
                return (Geometry) o;
            }
        }

        return null;
    }

    protected Schema buildSchema() {
        List<Field> fields = new ArrayList<Field>();
        int i = 0;
        boolean g = false;
        for (Object o : values) {
            if (o instanceof Geometry && !g) {
                //first gometry
                fields.add(new Field("geometry", o.getClass()));
                g = true;
            }
            else {
                //regular field
                fields.add(new Field(String.format("field%d", i++), o != null ? o.getClass():null));
            }
        }
        return new Schema("feature", fields);
    }

    @Override
    public List<Object> list() {
        return Collections.unmodifiableList(values);
    }

    @Override
    public Map<String, Object> map() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
        for (Field f : schema()) {
            map.put(f.getName(), get(f.getName()));
        }
        return map;
    }
}
