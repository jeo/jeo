package org.jeo.shp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jeo.data.Cursor;
import org.jeo.feature.Feature;
import org.jeo.feature.ListFeature;
import org.jeo.feature.Schema;
import org.jeo.shp.dbf.DbaseFileReader;
import org.jeo.shp.shp.ShapefileReader;
import org.jeo.shp.shp.ShapefileReader.Record;

import com.vividsolutions.jts.geom.Envelope;

public class ShpCursor extends Cursor<Feature> {

    ShpDataset shp;

    ShapefileReader shpReader;
    DbaseFileReader dbfReader;

    Envelope bbox;

    Feature next;

    ShpCursor(ShpDataset shp, Envelope bbox) throws IOException {
        this.shp = shp;
        this.shpReader = shp.newShpReader();
        this.dbfReader = shp.newDbfReader();

        this.bbox = bbox;
    }

    @Override
    public boolean hasNext() throws IOException {
        while(next == null && shpReader.hasNext()) {
            next = shp.next(shpReader, dbfReader, bbox);
        }
        return next != null;
    }

    @Override
    public Feature next() throws IOException {
        Feature f = next;
        next = null;
        return f;
    }

    @Override
    public void close() throws IOException {
        if (shpReader != null) {
            shpReader.close();
            shpReader = null;
        }
        if (dbfReader != null) {
            dbfReader.close();
            dbfReader = null;
        }
    }
}
