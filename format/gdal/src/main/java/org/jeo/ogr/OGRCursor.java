package org.jeo.ogr;

import java.io.IOException;

import org.gdal.ogr.DataSource;
import org.gdal.ogr.Layer;
import org.jeo.data.Cursor;
import org.jeo.feature.Feature;
import org.jeo.feature.Schema;

public class OGRCursor extends Cursor<Feature> {

    Layer layer;
    DataSource dataSource;
    Schema schema;
    org.gdal.ogr.Feature next;
    Feature curr;
    boolean complete;

    public OGRCursor(Layer layer, DataSource dataSource, Schema schema) {
        this.layer = layer;
        this.dataSource = dataSource;
        this.schema = schema;

        layer.ResetReading();
        complete = false;
    }

    @Override
    public boolean hasNext() throws IOException {
        if (complete) {
            return false;
        }

        if (curr != null) {
            return true;
        }

        next = layer.GetNextFeature();
        if (next == null) {
            complete = true;
            return false;
        }

        curr = new OGRFeature(next, schema);
        return true;
    }

    @Override
    public Feature next() throws IOException {
        try {
            return curr;
        }
        finally {
            curr = null;
        }
    }

    @Override
    public void close() throws IOException {
        layer.delete();
    }
}
