package org.jeo.postgis;

public class SQL {

    StringBuilder buf;

    public SQL() {
        buf = new StringBuilder();
    }

    public SQL(String sql) {
        buf = new StringBuilder(sql);
    }

    public SQL add(String s) {
        buf.append(s);
        return this;
    }

    public SQL add(int i) {
        buf.append(i);
        return this;
    }

    public SQL add(double d) {
        buf.append(d);
        return this;
    }

    public SQL name(String name) {
        buf.append("\"").append(name).append("\"");
        return this;
    }

    public SQL trim(int n) {
        buf.setLength(buf.length()-n);
        return this;
    }

    @Override
    public String toString() {
        return buf.toString();
    }
}
