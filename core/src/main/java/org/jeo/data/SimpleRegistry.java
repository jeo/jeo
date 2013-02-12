package org.jeo.data;

import java.util.HashMap;
import java.util.Map;

public class SimpleRegistry implements Registry {

    Map<String,Object> reg;

    public SimpleRegistry() {
        reg = new HashMap<String, Object>();
    }

    public void put(String key, Workspace workspace) {
        reg.put(key, workspace);
    }

    @Override
    public Workspace get(String key) {
        return get(key, Workspace.class);
    }

    <T> T get(String key, Class<T> clazz) {
        if (reg.containsKey(key)) {
            Object o = reg.get(key);
            if (clazz.isInstance(o)) {
                return clazz.cast(o);
            }
        }
        return null;
    }
}
