package org.jeo.shp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jeo.data.Cursor;
import org.jeo.data.Cursors;
import org.jeo.feature.Feature;
import org.jeo.feature.ListFeature;
import org.jeo.feature.Schema;
import org.jeo.shp.dbf.DbaseFileReader;
import org.jeo.shp.shp.ShapefileReader;
import org.jeo.shp.shp.ShapefileReader.Record;

import com.vividsolutions.jts.geom.Envelope;

public class ShapefileCursor implements Cursor<Feature> {

    ShapefileReader shpReader;
    DbaseFileReader dbfReader;

    Schema schema;
    Envelope bbox;

    Record next;

    ShapefileCursor(Shapefile shp, Envelope bbox) throws IOException {
        this.schema = shp.getSchema();
        this.shpReader = shp.newShpReader();
        this.dbfReader = shp.newDbfReader();

        this.bbox = bbox;
    }

    @Override
    public boolean hasNext() throws IOException {
        while (next == null && shpReader.hasNext()) {
            Record r = shpReader.nextRecord();
            if (bbox == null || bbox.intersects(r.envelope())) {
                next = r;
            }
        }
        return next != null;
    }

    @Override
    public Feature next() throws IOException {
        if (next == null) {
            return null;
        }
        List<Object> values = new ArrayList<Object>();

        values.add(next.shape());
        if (dbfReader.hasNext()) {
            for (Object o : dbfReader.readEntry()) {
                values.add(o);
            }
        }

        ListFeature f = new ListFeature(values, schema);
        next = null;

        return f;
    }

    @Override
    public void close() throws IOException {
        if (shpReader != null) shpReader.close();
        if (dbfReader != null) dbfReader.close();
    }


    @Override
    public Iterator<Feature> iterator() {
        return Cursors.iterator(this);
    }

}
