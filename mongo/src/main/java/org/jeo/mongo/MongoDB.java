package org.jeo.mongo;

import java.io.IOException;
import java.util.Iterator;

import org.jeo.data.Vector;
import org.jeo.data.Workspace;
import org.jeo.feature.Schema;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;

public class MongoDB implements Workspace {

    DB db;

    public MongoDB(DB db) {
        this.db = db;
    }

    @Override
    public Iterator<String> layers() throws IOException {
        return db.getCollectionNames().iterator();
    }

    @Override
    public MongoDataset get(String layer) throws IOException {
        DBCollection col = db.getCollection(layer);
        return col != null ? new MongoDataset(col) : null;
    }

    @Override
    public Vector create(Schema schema) throws IOException {
        db.createCollection(schema.getName(), new BasicDBObject());
        return get(schema.getName());
    }

    @Override
    public void dispose() {
    }

}
