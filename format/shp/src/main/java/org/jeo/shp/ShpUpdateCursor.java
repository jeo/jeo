package org.jeo.shp;

import java.io.IOException;

import org.jeo.data.Cursor;
import org.jeo.feature.Feature;
import org.jeo.shp.dbf.DbaseFileReader;
import org.jeo.shp.shp.ShapefileReader;

import com.vividsolutions.jts.geom.Envelope;

public class ShpUpdateCursor extends ShpWriteCursor {

    ShapefileReader shpReader;
    DbaseFileReader dbfReader;

    Envelope bbox;
    
    ShpUpdateCursor(ShpDataset shp, Envelope bbox) throws IOException {
        super(shp, Cursor.UPDATE);
        this.bbox = bbox;

        shpReader = shp.newShpReader();
        dbfReader = shp.newDbfReader();
    }

    @Override
    public boolean hasNext() throws IOException {
        while (next == null && shpReader.hasNext()) {
            next = shp.next(shpReader, dbfReader, bbox);
            if (next == null) {
                doWrite();
            }
        }
        return next != null;
    }

    @Override
    public Feature next() throws IOException {
        return next;
    }

    @Override
    protected void doRemove() throws IOException {
        // do nothing, don't write this feature out
        next = null;
    }

    @Override
    public void close() throws IOException {
        super.close();
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
