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

    public MongoWorkspace(MongoOpts mopts) throws IOException {
        this.mopts = mopts;
        this.db = mopts.connect();
    }
    
    MongoWorkspace(DB db) {
        this.db = db;
    }

    @Override
    public Driver<?> getDriver() {
        return new MongoDB();
    }

    @Override
    public Iterator<String> layers() throws IOException {
        Set<String> collections = db.getCollectionNames();
        for (Iterator<String> it = collections.iterator(); it.hasNext();) {
            String name = it.next();
            if (name.startsWith("system.")) {
                it.remove();
            }
        }
        return collections.iterator();
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
    public void dispose() {
    }

}
