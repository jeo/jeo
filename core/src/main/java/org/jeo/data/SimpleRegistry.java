package org.jeo.data;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A basic registry that stores workspace objects in a {@link HashMap}.
 * <p>
 * The {@link #put(String, Workspace)} method is used to register workspaces.
 * </p>
 * @author Justin Deoliveira, OpenGeo
 */
public class SimpleRegistry implements Registry {

    /** logger */
    static Logger LOG = LoggerFactory.getLogger(SimpleRegistry.class);

    /** registry map */
    Map<String,Workspace> reg;

    /**
     * Constructs a new empty registry.
     */
    public SimpleRegistry() {
        reg = new HashMap<String, Workspace>();
    }

    /**
     * Constructs a new registry from an existing map.
     */
    public SimpleRegistry(Map<String,Workspace> map) {
        reg = new HashMap<String, Workspace>(map);
    }

    @Override
    public Iterable<String> list() {
        return reg.keySet();
    }

    /**
     * Registers a new workspace.
     * 
     * @param key The name/key of the workspace.
     * @param workspace The workspace.
     */
    public void put(String key, Workspace workspace) {
        reg.put(key, workspace);
    }

    @Override
    public Workspace get(String key) {
        return reg.get(key);
    }

    public void close() {
        for (Workspace ws : reg.values()) {
            try {
                ws.close();
            }
            catch(Exception e) {
                LOG.warn("Error disposing workspace", e);
            }
        }
        reg.clear();
    }
}
