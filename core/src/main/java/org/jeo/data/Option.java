package org.jeo.data;


public class Option<T> {

    String key;
    
    public Option (String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
