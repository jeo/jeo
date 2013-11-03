package org.jeo.data.mem;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jeo.data.VectorDriver;
import org.jeo.feature.Schema;
import org.jeo.util.Key;
import org.jeo.util.Messages;

/**
 * Driver for in memory feature objects.
 * 
 * @author Justin Deoliveira, Boundless
 */
public class Memory implements VectorDriver<MemWorkspace> {

    public static MemWorkspace open() {
        return new MemWorkspace();
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
        return opts.isEmpty();
    }

    @Override
    public MemWorkspace open(Map<?, Object> opts) throws IOException {
        return new MemWorkspace();
    }

    @Override
    public boolean canCreate(Map<?, Object> opts, Messages msgs) {
        return opts.isEmpty();
    }

    @Override
    public MemWorkspace create(Map<?, Object> opts, Schema schema) throws IOException {
        MemWorkspace ws = new MemWorkspace();
        ws.create(schema);
        return ws;
    }
}
