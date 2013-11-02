package org.jeo.data.mem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jeo.data.DataRepository;
import org.jeo.data.Workspace;
import org.jeo.data.WorkspaceHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A basic registry that stores workspace objects in a {@link HashMap}.
 * <p>
 * The {@link #put(String, Workspace)} method is used to register workspaces.
 * </p>
 * @author Justin Deoliveira, OpenGeo
 */
public class MemRepository implements DataRepository {

    /** logger */
    static Logger LOG = LoggerFactory.getLogger(MemRepository.class);

    /** registry map */
    Map<String, Workspace> map;

    /**
     * Constructs a new empty repo.
     */
    public MemRepository() {
        map = new LinkedHashMap<String, Workspace>();
    }

    /**
     * Constructs a new repo from an existing map.
     */
    public MemRepository(Map<String,Workspace> map) {
        map = new LinkedHashMap<String, Workspace>(map);
    }

    public MemRepository(String name, Workspace workspace) {
        this(Collections.singletonMap(name, workspace));
    }

    @Override
    public Iterable<WorkspaceHandle> list() {
        List<WorkspaceHandle> items = new ArrayList<WorkspaceHandle>();
        for (Map.Entry<String, Workspace> kv : map.entrySet()) {
            Workspace ws = kv.getValue();
            items.add(new WorkspaceHandle(kv.getKey(), ws.getDriver(), this));
        }
        return items;
    }

    /**
     * Adds a new workspace to the repository.
     * 
     * @param key The name/key of the workspace.
     * @param workspace The workspace.
     */
    public void put(String key, Workspace ws) {
        map.put(key, ws);
    }

    @Override
    public Workspace get(String key) {
        return map.get(key);
    }

    public void close() {
        for (Workspace ws : map.values()) {
            ws.close();
        }
        map.clear();
    }
}
