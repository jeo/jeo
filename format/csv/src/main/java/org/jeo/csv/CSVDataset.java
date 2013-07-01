package org.jeo.csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jeo.data.Cursor;
import org.jeo.data.Cursors;
import org.jeo.data.Query;
import org.jeo.data.QueryPlan;
import org.jeo.data.VectorData;
import org.jeo.feature.Feature;
import org.jeo.feature.ListFeature;
import org.jeo.feature.Schema;
import org.jeo.feature.SchemaBuilder;
import org.jeo.util.Util;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

public class CSVDataset implements VectorData {

    /** the csv file */
    File file;

    /** csv stuff */
    CSVOpts opts;

    /** the feature schema */
    Schema schema;

    /** handler for specific csv flavor */
    CSVHandler handler;

    public CSVDataset(File file) throws IOException {
        this(file, new CSVOpts());
    }

    public CSVDataset(File file, CSVOpts opts) throws IOException {
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
        SchemaBuilder sb = Schema.build(getName());
        sb.field("geometry", Geometry.class);

        if (opts.hasHeader()) {
            //read first row
            BufferedReader r = reader();
            try {
                String[] row = row(r.readLine());
                handler.header(row);

                for (String col : row) {
                    sb.field(col, Object.class);
                }
            }
            finally {
                r.close();
            }
        }
        
        schema = sb.schema();
    }

    @Override
    public String getName() {
        return Util.base(file.getName());
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
        return Cursors.size(cursor(q));
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

        return new QueryPlan(q).apply(new CSVCursor(reader, this));
    }

    public void close() {
    }
    
    BufferedReader reader() throws FileNotFoundException {
        return new BufferedReader(new FileReader(file));
    }

    Feature feature(int i, String line) throws IOException {
        List<Object> row = parseRow(line);
        List<Object> values = new ArrayList<Object>();
        values.add(handler.geom(row));
        values.addAll(row);

        return new ListFeature(String.valueOf(i), values, schema);
    }

    String[] row(String line) {
        return opts.getDelimiter().split(line);
    }

    List<Object> parseRow(String line) {
        List<Object> row = new ArrayList<Object>();
        for (String val : row(line)) {
            Object parsed = null;
            try {
                parsed = Integer.parseInt(val);
            }
            catch(NumberFormatException e1) {
                try {
                    parsed = Double.parseDouble(val);
                }
                catch(NumberFormatException e2) {
                    parsed = val;
                }
            }
            row.add(parsed);
        }
        return row;
    }

}
