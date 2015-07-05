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
package io.jeo.data.mem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.jeo.data.Dataset;
import io.jeo.data.Workspace;
import io.jeo.data.Handle;
import io.jeo.vector.Schema;
import io.jeo.util.Key;

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
        this(Collections.singletonMap(dataset.name(), dataset));
    }
    public MemWorkspace(Map<String,Dataset> map) {
        this.map = new LinkedHashMap<String, Dataset>(map);
    }

    @Override
    public Memory driver() {
        return new Memory();
    }

    @Override
    public Map<Key<?>, Object> driverOptions() {
        return Collections.emptyMap();
    }

    @Override
    public Iterable<Handle<Dataset>> list() throws IOException {
        List<Handle<Dataset>> list = new ArrayList<Handle<Dataset>>();
        for (String key : map.keySet()) {
            list.add(Handle.to(key, this));
        }
        return list;
    }

    @Override
    public Dataset get(String layer) throws IOException {
        return map.get(layer);
    }

    public MemWorkspace put(String layer, Dataset dataset) {
        map.put(layer, dataset);
        return this;
    }

    @Override
    public MemVectorDataset create(Schema schema) throws IOException, UnsupportedOperationException {
        MemVectorDataset v = new MemVectorDataset(schema);
        map.put(schema.name(), v);
        return v;
    }

    public MemWorkspace remove(String layer) {
        map.remove(layer);
        return this;
    }

    /**
     * Clears the workspace.
     */
    public MemWorkspace clear() {
        map.clear();
        return this;
    }

    @Override
    public void close() {
    }
}
