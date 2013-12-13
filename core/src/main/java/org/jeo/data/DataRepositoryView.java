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
package org.jeo.data;

import java.io.IOException;
import org.jeo.filter.Filter;
import org.jeo.filter.Filters;

public class DataRepositoryView implements DataRepository {
    
    private final DataRepository repo;

    public DataRepositoryView(DataRepository repo) {
        this.repo = repo;
    }

    /**
     * Get all handles for objects in the repository. This includes
     * workspaces, their datasets and any styles.
     *
     * @return An Iterable of Handle objects for all contents.
     */
    public Iterable<Handle<?>> list() throws IOException {
        return repo.query(Filters.all());
    }

    @Override
    public Iterable<Handle<?>> query(Filter<? super Handle<?>> filter) throws IOException {
        return repo.query(filter);
    }

    @Override
    public <T> T get(String name, Class<T> type) throws IOException {
        return repo.get(name, type);
    }

    @Override
    public void close() {
        repo.close();
    }

}
