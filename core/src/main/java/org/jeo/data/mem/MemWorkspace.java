package org.jeo.data.mem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jeo.data.Dataset;
import org.jeo.data.DatasetHandle;
import org.jeo.data.Workspace;
import org.jeo.feature.Schema;
import org.jeo.util.Key;

/**
 * A repository that stores dataset objects in a {@link Map} in memory.
 * <p>
 * The {@link #put(String, Dataset)} method is used to register datasets.
 * </p>
 * @author Justin Deoliveira, OpenGeo
 */
public class MemWorkspace implements Workspace {

    Map<String,Dataset> map;

    public MemWorkspace() {
        this(Collections.EMPTY_MAP);
    }

    public MemWorkspace(Dataset dataset) {
        this(Collections.singletonMap(dataset.getName(), dataset));
    }
    public MemWorkspace(Map<String,Dataset> map) {
        this.map = new LinkedHashMap<String, Dataset>(map);
    }

    @Override
    public Memory getDriver() {
        return new Memory();
    }

    @Override
    public Map<Key<?>, Object> getDriverOptions() {
        return Collections.emptyMap();
    }

    @Override
    public Iterable<DatasetHandle> list() throws IOException {
        List<DatasetHandle> list = new ArrayList<DatasetHandle>();
        for (String key : map.keySet()) {
            list.add(new DatasetHandle(key, Dataset.class, getDriver(), this));
        }
        return list;
    }

    @Override
    public Dataset get(String layer) throws IOException {
        return map.get(layer);
    }

    public void put(String layer, Dataset dataset) {
        map.put(layer, dataset);
    }

    @Override
    public MemVector create(Schema schema) throws IOException, UnsupportedOperationException {
        MemVector v = new MemVector(schema);
        map.put(schema.getName(), v);
        return v;
    }

    @Override
    public void close() {
        map.clear();
    }
}
