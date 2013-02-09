package org.jeo.feature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.vividsolutions.jts.geom.Geometry;

public class Feature {

    Schema schema;
    Geometry geom;
    List<Object> values;

    public Feature() {
        this(null);
    }

    public Feature(Schema schema) {
        this(schema, null, new ArrayList<Object>());
    }

    public Feature(Schema schema, Geometry geom, Object... values) {
        this(schema, geom, Arrays.asList(values));
    }

    public Feature(Schema schema, Geometry geom, List<Object> values) {
        this.schema = schema;
        this.geom = geom;
        this.values = pad(values, schema);

    }

    List<Object> pad(List<Object> l, Schema schema) {
        l = l == null ? new ArrayList<Object>() : new ArrayList<Object>(l);

        if (schema != null) {
            while(l.size() < schema.getFields().size()) {
                l.add(null);
            }    
        }
        
        return l;
    }

    public Schema getSchema() {
        return schema;
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    public Geometry getGeometry() {
        return geom;
    }

    public void setGeometry(Geometry geom) {
        this.geom = geom;
    }

    public Object get(String name) {
        int i = schema.indexOf(name);
        return i != -1 ? values.get(i) : null;
    }

    public void put(String name, Object value) {
        int i = schema.indexOf(name);
        if (i != -1) {
            values.set(i, value);
        }
    }

    public List<Object> getValues() {
        return values;
    }
}
