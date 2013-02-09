package org.jeo.feature;

public class Field {

    String name;
    Class<?> type;

    public Field(String name, Class<?> type) {
        this.name = name;
        this.type = type != null ? type : Object.class;
    }

    public String getName() {
        return name;
    }

    public Class<?> getType() {
        return type;
    }

    @Override
    public String toString() {
        return String.format("(%s,%s)", name, getType().getSimpleName());
    }
}
