package org.jeo.data.mem;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jeo.data.DataRef;
import org.jeo.data.Dataset;
import org.jeo.data.Workspace;
import org.jeo.feature.Schema;
import org.jeo.util.Key;

public class MemWorkspace implements Workspace {

    Map<DataRef<Dataset>,Dataset> data = new LinkedHashMap<DataRef<Dataset>, Dataset>();

    @Override
    public Memory getDriver() {
        return new Memory();
    }

    @Override
    public Map<Key<?>, Object> getDriverOptions() {
        return Collections.emptyMap();
    }

    @Override
    public Iterable<DataRef<Dataset>> list() throws IOException {
        return data.keySet();
    }

    @Override
    public Dataset get(String layer) throws IOException {
        return data.get(new DataRef<Dataset>(Dataset.class, layer));
    }

    public void put(String layer, Dataset dataset) {
        data.put(new DataRef<Dataset>(Dataset.class, layer), dataset);
    }

    @Override
    public MemVector create(Schema schema) throws IOException, UnsupportedOperationException {
        MemVector v = new MemVector(schema);
        data.put(new DataRef<Dataset>(Dataset.class, schema.getName()), v);
        return v;
    }

    @Override
    public void close() {
        data.clear();
    }
}
