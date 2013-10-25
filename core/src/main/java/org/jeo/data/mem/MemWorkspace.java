package org.jeo.data.mem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jeo.data.DataRef;
import org.jeo.data.Dataset;
import org.jeo.data.Workspace;
import org.jeo.feature.Schema;
import org.jeo.util.Key;

public class MemWorkspace implements Workspace {

    Map<String,Dataset> data = new LinkedHashMap<String, Dataset>();

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
        List<DataRef<Dataset>> list = new ArrayList<DataRef<Dataset>>();
        for (String key : data.keySet()) {
            list.add(new DataRef<Dataset>(key, Dataset.class, getDriver(), this));
        }
        return list;
    }

    @Override
    public Dataset get(String layer) throws IOException {
        return data.get(layer);
    }

    public void put(String layer, Dataset dataset) {
        data.put(layer, dataset);
    }

    @Override
    public MemVector create(Schema schema) throws IOException, UnsupportedOperationException {
        MemVector v = new MemVector(schema);
        data.put(schema.getName(), v);
        return v;
    }

    @Override
    public void close() {
        data.clear();
    }
}
