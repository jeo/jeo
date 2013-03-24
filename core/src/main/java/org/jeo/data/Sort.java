package org.jeo.data;

public class Sort {

    String property;
    boolean ascending = true;

    public Sort(String property) {
        if (property.startsWith("-")) {
            ascending = false;
            property = property.substring(1);
        }
        else if (property.startsWith("+")){
            ascending = true;
            property = property.substring(1);
        }

        this.property = property;
    }

    public String getProperty() {
        return property;
    }

    public boolean isAscending() {
        return ascending;
    }
}
