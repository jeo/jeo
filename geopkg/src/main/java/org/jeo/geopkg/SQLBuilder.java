package org.jeo.geopkg;

public class SQLBuilder {

    StringBuilder buf;

    public SQLBuilder() {
        buf = new StringBuilder();
    }

    public SQLBuilder(String sql) {
        buf = new StringBuilder(sql);
    }

    public SQLBuilder add(String s) {
        buf.append(s);
        return this;
    }

    public SQLBuilder add(int i) {
        buf.append(i);
        return this;
    }

    public SQLBuilder add(double d) {
        buf.append(d);
        return this;
    }

    public SQLBuilder name(String name) {
        buf.append("\"").append(name).append("\"");
        return this;
    }

    public SQLBuilder trim(int n) {
        buf.setLength(buf.length()-n);
        return this;
    }

    @Override
    public String toString() {
        return buf.toString();
    }
}
