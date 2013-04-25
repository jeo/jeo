package org.jeo.csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jeo.data.Cursor;
import org.jeo.data.Cursors;
import org.jeo.data.Query;
import org.jeo.data.Vector;
import org.jeo.feature.Feature;
import org.jeo.feature.Field;
import org.jeo.feature.ListFeature;
import org.jeo.feature.Schema;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

public class CSVDataset implements Vector {

    /** the csv file */
    File file;

    /** csv stuff */
    CSVOptions opts;

    /** the feature schema */
    Schema schema;

    /** handler for specific csv flavor */
    CSVHandler handler;

    public CSVDataset(File file) throws IOException {
        this(file, new CSVOptions());
    }

    public CSVDataset(File file, CSVOptions opts) throws IOException {
        this.file = file;
        this.opts = opts;
        handler = opts.handler();
        init();
    }

    @Override
    public CSV getDriver() {
        return new CSV();
    }

    void init() throws IOException {
        List<Field> fields = new ArrayList<Field>();
        fields.add(new Field("geometry", Geometry.class));

        if (opts.hasHeader()) {
            //read first row
            BufferedReader r = reader();
            try {
                String[] row = row(r.readLine());
                fields.addAll(handler.header(row));
            }
            finally {
                r.close();
            }
        }

        schema = new Schema(getName(), fields);
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public String getTitle() {
        return getName();
    }

    @Override
    public String getDescription() {
        return getTitle();
    }

    @Override
    public Schema getSchema() throws IOException {
        return schema; 
    }

    @Override
    public CoordinateReferenceSystem getCRS() {
        return null;
    }

    @Override
    public Envelope bounds() throws IOException {
        return Cursors.extent(cursor(new Query()));
    }

    @Override
    public long count(Query q) throws IOException {
        return Cursors.size(cursor(new Query()));
    }

    @Override
    public Cursor<Feature> cursor(Query q) throws IOException {
        if (q.getMode() != Cursor.READ) {
            throw new IllegalArgumentException("write cursors not supported");
        }

        BufferedReader reader = reader();
        if (opts.hasHeader()) {
            reader.readLine();
        }

        Cursor<Feature> c = new CSVCursor(reader, this);
        if (q.getBounds() != null) {
            c = Cursors.intersects(c, q.getBounds());
        }
        return q.apply(c);
    }

    public void dispose() {
    }
    
    BufferedReader reader() throws FileNotFoundException {
        return new BufferedReader(new FileReader(file));
    }

    Feature feature(int i, String line) {
        String[] row = row(line);
        List<Object> values = new ArrayList<Object>();
        values.add(handler.geom(row));
        values.addAll(Arrays.asList(row));

        return new ListFeature(String.valueOf(i), values, schema);
    }

    String[] row(String line) {
        return opts.getDelimiter().split(line);
    }

    
}
