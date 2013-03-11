package org.jeo.map;

import org.jeo.filter.Filter;

public class Selector {

    public static enum Type {
        NAME, ID, CLASS;
    }

    String name;
    Type type;

    String psuedo;

    Filter<?> filter = Filter.TRUE;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPsuedo() {
        return psuedo;
    }

    public void setPsuedo(String psuedo) {
        this.psuedo = psuedo;
    }

    public Filter<?> getFilter() {
        return filter;
    }

    public void setFilter(Filter<?> filter) {
        this.filter = filter;
    }
}
