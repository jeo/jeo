package io.jeo.postgis;

import io.jeo.vector.DiffFeature;
import io.jeo.vector.Feature;
import io.jeo.vector.FeatureCursor;
import io.jeo.vector.FeatureWriteCursor;

import java.io.IOException;
import java.sql.Connection;

public class PostGISUpdateCursor extends FeatureWriteCursor {

    FeatureCursor cursor;
    PostGISDataset dataset;
    Connection cx;

    DiffFeature next;

    public PostGISUpdateCursor(FeatureCursor cursor, Connection cx, PostGISDataset dataset) {
        this.cursor = cursor;
        this.dataset = dataset;
        this.cx = cx;
    }

    @Override
    public boolean hasNext() throws IOException {
        return cursor.hasNext();
    }

    @Override
    public Feature next() throws IOException {
        return next = new DiffFeature(cursor.next());
    }

    @Override
    public FeatureWriteCursor write() throws IOException {
        dataset.doUpdate(next, next.changed(), cx);
        return this;
    }

    @Override
    public FeatureWriteCursor remove() throws IOException {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() throws IOException {

    }
}
