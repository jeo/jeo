package org.jeo.carto;

public class Property {

    String key;
    Object value;

    public Property(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
