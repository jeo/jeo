package org.jeo.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for workspaces.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class Workspaces {

    /** logger */
    static Logger LOG = LoggerFactory.getLogger(Workspaces.class);

    /** list of registred factories */
    static List<WorkspaceFactory<?>> factories = new ArrayList<WorkspaceFactory<?>>();

    /**
     * Registers a workspace factory.
     * <p>
     * A {@link Workspace} implementation should call this method, typically from a static 
     * initializer block.
     * </p>
     */
    public static void registerWorkspaceFactory(WorkspaceFactory<?> factory) {
        factories.add(factory);
    }

    /**
     * Creates a workspace from a set of well known named parameters.
     *
     * @param map The well known workspace parameters.
     * 
     * @return The workspace instance, or <code>null</code> if none could be created.
     */
    public static Workspace create(Map<String,Object> map) {
        for (WorkspaceFactory<?> f : factories) {
            Workspace w;
            try {
                w = f.create(map);
                if (w != null) {
                    return w;
                }
            } catch (IOException e) {
                LOG.warn("Error creating workspace", e);
            }
        }
        return null;
    }

    /**
     * Creates a workspace from a file.
     * 
     * @param file The file.
     * 
     * @return The workspace instance, or <code>null</code> if none could be created.
     */
    public static Workspace create(File file) {
        Map<String,Object> map = new HashMap<String, Object>();
        map.put(FileWorkspaceFactory.FILE, file);
        return create(map);
    }
}
