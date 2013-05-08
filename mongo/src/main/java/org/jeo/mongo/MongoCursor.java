package org.jeo.mongo;

import java.io.IOException;

import org.jeo.data.Cursor;
import org.jeo.feature.Feature;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class MongoCursor extends Cursor<Feature> {

    DBCursor dbCursor;
    MongoMapper mapper;
    MongoDataset dataset;

    Feature next;

    MongoCursor(Mode mode, DBCursor dbCursor, MongoDataset dataset) {
        super(mode);
        this.dbCursor = dbCursor;
        this.dataset = dataset;
        this.mapper = dataset.getMapper();
    }

    @Override
    public boolean hasNext() throws IOException {
        return mode == APPEND || dbCursor.hasNext();
    }

    @Override
    public Feature next() throws IOException {
        return next = mapper.feature(mode == APPEND ? new BasicDBObject() : dbCursor.next());
    }

    @Override
    protected void doRemove() throws IOException {
        dbCursor.getCollection().remove(mapper.object(next));
    }

    @Override
    protected void doWrite() throws IOException {
        //TODO: check write result
        if (getMode() == APPEND) {
            DBObject obj = mapper.object(next);
            dataset.getCollection().insert(obj);
        }
        else {
            DBObject obj = mapper.object(next);
            dataset.getCollection().update(new BasicDBObject("_id", obj.get("_id")), obj);
        }
    }

    @Override
    public void close() throws IOException {
        dbCursor.close();
    }

    
}
