package org.jeo.mongo;

import java.io.IOException;

import org.jeo.data.Cursor;
import org.jeo.feature.Feature;

import com.mongodb.DBCursor;

public class MongoCursor extends Cursor<Feature> {

    DBCursor dbCursor;
    MongoMapper mapper;

    MongoCursor(DBCursor dbCursor, MongoMapper mapping) {
        this.dbCursor = dbCursor;
        this.mapper = mapping;
    }

    @Override
    public boolean hasNext() throws IOException {
        return dbCursor.hasNext();
    }

    @Override
    public Feature next() throws IOException {
        return mapper.feature(dbCursor.next());
    }

    @Override
    public void close() throws IOException {
        dbCursor.close();
    }
}
