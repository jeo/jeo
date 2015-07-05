package io.jeo.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import io.jeo.vector.Feature;
import io.jeo.vector.FeatureCursor;
import io.jeo.vector.FeatureWriteCursor;

import java.io.IOException;

public class MongoUpdateCursor extends FeatureWriteCursor {

    FeatureCursor cursor;
    MongoDataset dataset;

    Feature next;

    public MongoUpdateCursor(FeatureCursor cursor, MongoDataset dataset) {
        this.cursor = cursor;
        this.dataset = dataset;
    }

    @Override
    public boolean hasNext() throws IOException {
        return cursor.hasNext();
    }

    @Override
    public Feature next() throws IOException {
        return next = cursor.next();
    }

    @Override
    public MongoUpdateCursor write() throws IOException {
        DBObject obj = dataset.mapper().object(next, dataset);
        dataset.getCollection().update(new BasicDBObject("_id", obj.get("_id")), obj);
        return this;
    }

    @Override
    public MongoUpdateCursor remove() throws IOException {
        DBObject obj = dataset.mapper().object(next, dataset);
        dataset.getCollection().remove(obj);
        return this;
    }

    @Override
    public void close() throws IOException {
        if (cursor != null) {
            cursor.close();
            cursor = null;
        }
    }
}
