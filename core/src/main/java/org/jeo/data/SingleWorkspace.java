package org.jeo.data;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.jeo.feature.Schema;
import org.jeo.util.Key;

/**
 * Workspace implementation that contains a single dataset instance.
 * 
 * @author Justin Deoliveira, Boundless
 */
public class SingleWorkspace implements Workspace {

    Dataset dataset;

    public SingleWorkspace(Dataset dataset) {
        this.dataset = dataset;
    }

    @Override
    public Driver<?> getDriver() {
        return dataset.getDriver();
    }
    
    @Override
    public Map<Key<?>, Object> getDriverOptions() {
        return dataset.getDriverOptions();
    }
    
    @Override
    public Iterable<DatasetHandle> list() throws IOException {
        return Arrays.asList(new DatasetHandle(dataset.getName(), dataset.getClass(), 
            dataset.getDriver(), this));
    }
    
    @Override
    public Dataset get(String layer) throws IOException {
        if (dataset.getName().equals(layer)) {
            return dataset;
        }
        return null;
    }
    
    @Override
    public VectorDataset create(Schema schema) throws IOException {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void close() {
        dataset.close();
    }
}
