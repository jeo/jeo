package org.jeo.csv;

public class Delimiter {

    public static Delimiter comma() {
        return new Delimiter(" *, *");
    }

    public static Delimiter whitespace() {
        return new Delimiter(" +");
    }

    String regex;

    public Delimiter() {
        this(",");
    }

    public Delimiter(String regex) {
        this.regex = regex;
    }

    public String[] split(String row) {
        return row.split(regex);
    }
}
