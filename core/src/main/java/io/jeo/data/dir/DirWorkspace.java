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
package io.jeo.data.dir;

import io.jeo.data.Dataset;
import io.jeo.data.Driver;
import io.jeo.data.Drivers;
import io.jeo.data.FileData;
import io.jeo.data.Handle;
import io.jeo.data.Workspace;
import io.jeo.util.Key;
import io.jeo.util.Util;
import io.jeo.vector.Schema;
import io.jeo.vector.VectorDataset;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class DirWorkspace implements Workspace, FileData {

    File dir;

    public DirWorkspace(File dir) {
        this.dir = dir;
    }

    @Override
    public File file() {
        return dir;
    }

    @Override
    public Driver<?> driver() {
        return new Directory();
    }

    @Override
    public Map<Key<?>, Object> driverOptions() {
        return (Map) Collections.singletonMap(Directory.FILE, dir);
    }

    @Override
    public Iterable<Handle<Dataset>> list() throws IOException {
        List<Handle<Dataset>> list = new ArrayList<>();
        for (FileGroup group : fileGroups().values()) {
            if (group.driver() != null) {
                list.add(Handle.to(group.basename(), this));
            }
        }
        return list;
    }

    @Override
    public Dataset get(String name) throws IOException {
        FileGroup group = fileGroups().get(name);
        if (group == null) {
            return null;
        }

        return Drivers.open(group.main(), Dataset.class);
    }

    @Override
    public VectorDataset create(Schema schema) throws IOException {
        // use geojson?
        throw new UnsupportedOperationException();
    }

    @Override
    public void destroy(String name) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
    }

    public Map<String,FileGroup> fileGroups() {
        Map<String,FileGroup> groups = new LinkedHashMap<>();
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                continue; // TODO: handle directories recursively
            }
            String base = Util.base(f.getName());
            FileGroup group = groups.get(base);
            if (group == null) {
                group = new FileGroup();
                groups.put(base, group);
            }
            group.add(f);
        }

        Iterator<Entry<String,FileGroup>> it = groups.entrySet().iterator();
    O:  while (it.hasNext()) {
            FileGroup group = it.next().getValue();
            for (File f : group.files()) {
                Driver drv = Drivers.find(f.toURI());
                if (drv != null) {
                    group.driver(drv).main(f);
                    continue O;
                }
            }

            //it.remove();
        }

        return groups;
    }
}
