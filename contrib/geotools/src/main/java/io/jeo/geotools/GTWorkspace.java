/* Copyright 2015 The jeo project. All rights reserved.
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
package io.jeo.geotools;

import io.jeo.data.Dataset;
import io.jeo.data.Driver;
import io.jeo.data.Handle;
import io.jeo.data.Workspace;
import io.jeo.util.Key;
import io.jeo.vector.Schema;
import org.geotools.data.DataStore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GTWorkspace implements Workspace {

    DataStore store;
    GTVectorDriver driver;

    public GTWorkspace(DataStore store, GTVectorDriver driver) {
        this.store = store;
        this.driver = driver;
    }

    @Override
    public Driver<?> driver() {
        return driver;
    }

    @Override
    public Map<Key<?>, Object> driverOptions() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Handle<Dataset>> list() throws IOException {
        List<Handle<Dataset>> list = new ArrayList<>();
        for (String t : store.getTypeNames()) {
            list.add(Handle.to(t, this));
        }
        return list;
    }

    @Override
    public GTVectorDataset get(String name) throws IOException {
        return new GTVectorDataset(store.getFeatureSource(name), this);
    }

    @Override
    public GTVectorDataset create(Schema schema) throws IOException {
        // TODO: implement
        throw new UnsupportedOperationException();
    }

    @Override
    public void destroy(String name) throws IOException {
        // TODO: implement
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        if (store != null) {
            store.dispose();
            store = null;
        }
    }
}
