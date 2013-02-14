package org.jeo.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Workspaces {

    static Logger LOG = LoggerFactory.getLogger(Workspaces.class);

    static List<WorkspaceFactory<?>> factories = new ArrayList<WorkspaceFactory<?>>();

    public static void registerWorkspaceFactory(WorkspaceFactory<?> factory) {
        factories.add(factory);
    }

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

    public static Workspace create(File file) {
        Map<String,Object> map = new HashMap<String, Object>();
        map.put(FileWorkspaceFactory.FILE, file);
        return create(map);
    }
}
