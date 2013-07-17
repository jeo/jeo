package org.jeo.carto;

import org.jeo.filter.Expression;
import org.jeo.filter.Mixed;

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

    public void setValue(Expression expr) {
        if (value != null) {
            if (value instanceof Mixed) {
                value = ((Mixed) value).append(expr);
            }
            else {
                value = new Mixed(value, expr);
            }
        }
        else {
            value = expr;
        }
    }
}
