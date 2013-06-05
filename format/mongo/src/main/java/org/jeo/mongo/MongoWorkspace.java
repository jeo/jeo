package org.jeo.mongo;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import org.jeo.data.Driver;
import org.jeo.data.Workspace;
import org.jeo.feature.Schema;

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
    public Iterable<String> list() throws IOException {
        Set<String> collections = db.getCollectionNames();
        for (Iterator<String> it = collections.iterator(); it.hasNext();) {
            String name = it.next();
            if (name.startsWith("system.")) {
                it.remove();
            }
        }
        return collections;
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
