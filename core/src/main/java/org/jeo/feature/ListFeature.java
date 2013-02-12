package org.jeo.feature;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Geometry;

public class ListFeature extends Feature {

    List<Object> values;

    public ListFeature(List<Object> values) {
        this(values, null);
    }

    public ListFeature(List<Object> values, Schema schema) {
        super(schema);
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
    public List<Object> values() {
        return values;
    }
}
