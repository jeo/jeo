package org.jeo.data;

import java.util.HashMap;
import java.util.Map;

public class SimpleRegistry implements Registry {

    Map<String,Layer> reg;

    public SimpleRegistry() {
        reg = new HashMap<String, Layer>();
    }

    public void put(String key, Layer layer) {
        reg.put(key, layer);
    }

    @Override
    public Layer get(String key) {
        return reg.get(key);
    }

}
