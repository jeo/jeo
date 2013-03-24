package org.jeo.data.mem;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jeo.data.Dataset;
import org.jeo.data.Vector;
import org.jeo.data.Workspace;
import org.jeo.feature.Schema;

public class Memory implements Workspace {

    Map<String,Dataset> data = new LinkedHashMap<String, Dataset>();

    @Override
    public Iterator<String> layers() throws IOException {
        return data.keySet().iterator();
    }

    @Override
    public Dataset get(String layer) throws IOException {
        return data.get(layer);
    }

    @Override
    public MemoryVector create(Schema schema) throws IOException, UnsupportedOperationException {
        MemoryVector v = new MemoryVector(schema);
        data.put(schema.getName(), v);
        return v;
    }

    @Override
    public void dispose() {
        data.clear();
    }
}
