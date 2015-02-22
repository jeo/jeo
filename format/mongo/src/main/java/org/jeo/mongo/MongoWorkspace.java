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
package org.jeo.mongo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jeo.data.Dataset;
import org.jeo.data.Driver;
import org.jeo.data.Handle;
import org.jeo.data.Workspace;
import org.jeo.vector.Schema;
import org.jeo.util.Key;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;

public class MongoWorkspace implements Workspace {

    MongoOpts mopts;
    DB db;
    MongoMapper mapper;

    public MongoWorkspace(MongoOpts mopts) throws IOException {
        this(mopts.connect());
        this.mopts = mopts;
    }

    MongoWorkspace(DB db) {
        this.db = db;
        this.mapper = new DefaultMapper();
    }

    public MongoMapper getMapper() {
        return mapper;
    }

    public void setMapper(MongoMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Driver<?> driver() {
        return new MongoDB();
    }

    @Override
    public Map<Key<?>, Object> driverOptions() {
        return mopts.toMap();
    }

    @Override
    public Iterable<Handle<Dataset>> list() throws IOException {
        List<Handle<Dataset>> refs = new ArrayList<Handle<Dataset>>(); 
        for (String name : db.getCollectionNames()) {
            if (!name.startsWith("system.")) {
                refs.add(new Handle<Dataset>(name, Dataset.class, driver()) {
                    @Override
                    protected Dataset doResolve() throws IOException {
                        return get(name);
                    }
                });
            }
        }
        return refs;
    }

    @Override
    public MongoDataset get(String layer) throws IOException {
        DBCollection col = db.getCollection(layer);
        return col != null ? new MongoDataset(col, this) : null;
    }

    @Override
    public MongoDataset create(Schema schema) throws IOException {
        db.createCollection(schema.getName(), new BasicDBObject());
        return get(schema.getName());
    }

    @Override
    public void close() {
    }

}
