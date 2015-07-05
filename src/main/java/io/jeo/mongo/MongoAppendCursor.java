package io.jeo.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import io.jeo.vector.Feature;
import io.jeo.vector.FeatureAppendCursor;

import java.io.IOException;

public class MongoAppendCursor extends FeatureAppendCursor {

    MongoDataset dataset;
    MongoMapper mapper;
    Feature next;

    MongoAppendCursor(MongoDataset dataset) {
        this.dataset = dataset;
        this.mapper = dataset.mapper();
    }

    @Override
    public Feature next() throws IOException {
        return next = dataset.mapper().feature(new BasicDBObject(), dataset);
    }

    @Override
    public MongoAppendCursor write() throws IOException {
        DBObject obj = mapper.object(next, dataset);
        dataset.getCollection().insert(obj);
        return this;
    }

    @Override
    public void close() {

    }
}
