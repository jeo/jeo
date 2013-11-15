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
    Feature next;

    public OGRCursor(Layer layer, DataSource dataSource, Schema schema) {
        this.layer = layer;
        this.dataSource = dataSource;
        this.schema = schema;

        layer.ResetReading();
    }

    @Override
    public boolean hasNext() throws IOException {
        if (next != null) {
            return true;
        }

        org.gdal.ogr.Feature f = layer.GetNextFeature();
        if (f != null) {
            next = new OGRFeature(f, schema);
            return true;
        }

        return false;
    }

    @Override
    public Feature next() throws IOException {
        try {
            return next;
        }
        finally {
            next = null;
        }
    }

    @Override
    public void close() throws IOException {
        layer.delete();
    }
}
