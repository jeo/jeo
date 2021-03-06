/* Copyright 2013 The jeo project. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jeo.data;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import io.jeo.vector.Schema;
import io.jeo.util.Key;
import io.jeo.vector.VectorDataset;

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
    public Driver<?> driver() {
        return dataset.driver();
    }
    
    @Override
    public Map<Key<?>, Object> driverOptions() {
        return dataset.driverOptions();
    }
    
    @Override
    public Iterable<Handle<Dataset>> list() throws IOException {
        return Arrays.asList(Handle.to(dataset));
    }

    @Override
    public Dataset get(String layer) throws IOException {
        if (dataset.name().equals(layer)) {
            return dataset;
        }
        return null;
    }
    
    @Override
    public VectorDataset create(Schema schema) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void destroy(String name) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        dataset.close();
    }
}
