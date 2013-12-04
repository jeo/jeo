package org.jeo.mongo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jeo.data.Dataset;
import org.jeo.data.Driver;
import org.jeo.data.Handle;
import org.jeo.data.Workspace;
import org.jeo.feature.Schema;
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
    public Driver<?> getDriver() {
        return new MongoDB();
    }

    @Override
    public Map<Key<?>, Object> getDriverOptions() {
        return mopts.toMap();
    }

    @Override
    public Iterable<Handle<Dataset>> list() throws IOException {
        List<Handle<Dataset>> refs = new ArrayList<Handle<Dataset>>(); 
        for (String name : db.getCollectionNames()) {
            if (!name.startsWith("system.")) {
                refs.add(new Handle<Dataset>(name, Dataset.class, getDriver()) {
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
