package org.jeo.csv;

public class Delimiter {

    public static Delimiter comma() {
        return character(',');
    }

    public static Delimiter character(char ch) {
        return new Delimiter(" *" + ch + " *");
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

    @Override
    public String toString() {
        return regex;
    }
}
