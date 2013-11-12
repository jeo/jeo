package org.jeo.data.mem;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jeo.data.VectorDriver;
import org.jeo.feature.Schema;
import org.jeo.util.Key;
import org.jeo.util.Messages;

/**
 * Driver for in memory workspace objects.
 * <p>
 * Not meant to be used as a true format, mostly for testing purposes. The driver maintains a static
 * map of Workspace objects which are loadable by {@link #NAME}.
 * </p>
 * 
 * @author Justin Deoliveira, Boundless
 */
public class Memory implements VectorDriver<MemWorkspace> {

    /**
     * Name of the workspace, may be null to signify the default workspace. 
     */
    public static final Key<String> NAME = new Key<String>("name", String.class);

    static Map<String,MemWorkspace> WORKSPACES = new ConcurrentHashMap<String, MemWorkspace>();

    public static MemWorkspace open() {
        return Memory.open((String)null);
    }

    public static MemWorkspace open(String name) {
        return new Memory().open((Map) Collections.singletonMap(NAME, name));
    }
    
    @Override
    public String getName() {
        return "Memory";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("mem");
    }

    @Override
    public Class<MemWorkspace> getType() {
        return MemWorkspace.class;
    }

    @Override
    public List<Key<?>> getKeys() {
        return Collections.emptyList();
    }

    @Override
    public boolean canOpen(Map<?, Object> opts, Messages messages) {
        return opts.isEmpty() || (opts.size() == 1 && NAME.has(opts));
    }

    @Override
    public MemWorkspace open(Map<?, Object> opts)  {
        String name = NAME.get(opts);
        name = name == null ? "" : name;

        MemWorkspace ws = WORKSPACES.get(name);
        if (ws != null) {
            return ws;
        }

        synchronized (this) {
            WORKSPACES.put(name, new MemWorkspace());
        }

        return WORKSPACES.get(name);
    }

    @Override
    public boolean canCreate(Map<?, Object> opts, Messages msgs) {
        return canOpen(opts, msgs);
    }

    @Override
    public MemWorkspace create(Map<?, Object> opts, Schema schema) throws IOException {
        MemWorkspace ws = open(opts);
        ws.create(schema);
        return ws;
    }
}
