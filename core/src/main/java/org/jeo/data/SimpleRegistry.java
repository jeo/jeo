package org.jeo.data;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    Map<String,Object> reg;

    /**
     * Constructs a new empty registry.
     */
    public SimpleRegistry() {
        reg = new HashMap<String, Object>();
    }

    /**
     * Constructs a new registry from an existing map.
     */
    public SimpleRegistry(Map<String,Object> map) {
        reg = new HashMap<String, Object>(map);
    }

    @Override
    public Iterable<DataRef<?>> list() {
        List<DataRef<?>> items = new ArrayList<DataRef<?>>();
        for (String name : reg.keySet()) {
            Object obj = reg.get(name);
            Driver<?> drv = null;
            if (obj instanceof Dataset) {
                drv = ((Dataset) obj).getDriver();
            }
            else if (obj instanceof Workspace) {
                drv = ((Workspace)obj).getDriver();
            }

            items.add(new DataRef(name, obj.getClass(), drv, this));
        }
        return items;
    }

    /**
     * Registers a new workspace.
     * 
     * @param key The name/key of the workspace.
     * @param workspace The workspace.
     */
    public void put(String key, Object obj) {
        reg.put(key, obj);
    }

    @Override
    public Object get(String key) {
        return reg.get(key);
    }

    public void close() {
        for (Object obj : reg.values()) {
            if (obj instanceof Closeable) {
                try {
                    ((Closeable) obj).close();
                }
                catch(Exception e) {
                    LOG.warn("Error disposing obj", e);
                }
            }
        }
        reg.clear();
    }
}
