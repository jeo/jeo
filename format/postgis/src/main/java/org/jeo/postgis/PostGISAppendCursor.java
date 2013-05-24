package org.jeo.postgis;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.jeo.data.Cursor;
import org.jeo.feature.Feature;
import org.jeo.feature.ListFeature;

public class PostGISAppendCursor extends Cursor<Feature> {

    PostGISDataset dataset;
    Connection cx;

    Feature next;

    PostGISAppendCursor(PostGISDataset dataset, Connection cx) {
        super(Cursor.APPEND);
        this.dataset = dataset;
        this.cx = cx;
    }

    @Override
    public boolean hasNext() throws IOException {
        return true;
    }
    
    @Override
    public Feature next() throws IOException {
        return next = new ListFeature(null, null, dataset.getSchema());
    }

    @Override
    protected void doWrite() throws IOException {
        dataset.doInsert(next, cx);
    }

    @Override
    public void close() throws IOException {
        if (cx != null) {
            try {
                cx.close();
            } catch (SQLException e) {
                throw new IOException(e);
            }
        }
        cx = null;
    }
    

}
