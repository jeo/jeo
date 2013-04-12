package org.jeo.carto;

import org.jeo.filter.Expression;

public class Prop {

    String key;
    Expression value;

    public Prop(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public Expression getValue() {
        return value;
    }

    public void setValue(Expression value) {
        this.value = value;
    }
}
