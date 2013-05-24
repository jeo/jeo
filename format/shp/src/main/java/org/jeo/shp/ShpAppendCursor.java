package org.jeo.shp;

import java.io.IOException;

import org.jeo.data.Cursor;
import org.jeo.feature.Feature;
import org.jeo.feature.ListFeature;

public class ShpAppendCursor extends ShpWriteCursor {

    ShpAppendCursor(ShpDataset shp) throws IOException {
        super(shp, Cursor.APPEND);
    }

    @Override
    public boolean hasNext() throws IOException {
        return true;
    }

    @Override
    public Feature next() throws IOException {
        return next = new ListFeature(null, null, shp.getSchema());
    }
}
