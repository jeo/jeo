package org.jeo.geopkg;

import static org.jeo.geopkg.GeoPkgWorkspace.LOG;

import java.io.IOException;
import java.sql.Connection;

import org.jeo.data.Cursor;
import org.jeo.feature.Feature;
import org.jeo.feature.ListFeature;

public class FeatureAppendCursor extends Cursor<Feature> {

    Connection cx;

    FeatureEntry entry;
    GeoPkgWorkspace ws;

    Feature next;

    public FeatureAppendCursor(Connection cx, FeatureEntry entry, GeoPkgWorkspace ws) {
        super(Mode.APPEND);
        this.cx = cx;
        this.entry = entry;
        this.ws = ws;
    }

    @Override
    public boolean hasNext() throws IOException {
        return true;
    }

    @Override
    public Feature next() throws IOException {
        return next = new ListFeature(null, null, ws.schema(entry));
    }

    @Override
    protected void doWrite() throws IOException {
        ws.add(entry, next);
    }

    @Override
    public void close() throws IOException {
        if (cx != null) {
            try {
                cx.close();
            }
            catch(Exception e) {
                LOG.debug("error closing Connection", e);
            }
        }
    }
}
