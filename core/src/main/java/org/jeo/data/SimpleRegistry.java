package org.jeo.data;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleRegistry implements Registry {

    static Logger LOG = LoggerFactory.getLogger(SimpleRegistry.class);

    Map<String,Workspace> reg;

    public SimpleRegistry() {
        reg = new HashMap<String, Workspace>();
    }

    @Override
    public Iterator<String> keys() {
        return reg.keySet().iterator();
    }

    public void put(String key, Workspace workspace) {
        reg.put(key, workspace);
    }

    @Override
    public Workspace get(String key) {
        return reg.get(key);
    }

    public void dispose() {
        for (Workspace ws : reg.values()) {
            try {
                ws.dispose();
            }
            catch(Exception e) {
                LOG.warn("Error disposing workspace", e);
            }
        }
        reg.clear();
    }
}
