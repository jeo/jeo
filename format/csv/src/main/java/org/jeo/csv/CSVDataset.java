package org.jeo.csv;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.jeo.data.Cursor;
import org.jeo.data.Cursors;
import org.jeo.data.FileData;
import org.jeo.data.Query;
import org.jeo.data.QueryPlan;
import org.jeo.data.VectorDataset;
import org.jeo.feature.BasicFeature;
import org.jeo.feature.Feature;
import org.jeo.feature.Schema;
import org.jeo.feature.SchemaBuilder;
import org.jeo.util.Key;
import org.jeo.util.Util;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

public class CSVDataset implements VectorDataset, FileData {

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

    @Override
    public Map<Key<?>, Object> getDriverOptions() {
        return opts.toMap();
    }

    @Override
    public File getFile() {
        return file;
    }

    void init() throws IOException {
        SchemaBuilder sb = Schema.build(getName());
        sb.field("geometry", Geometry.class);

        if (opts.hasHeader()) {
            //read first row
            BufferedReader r = reader();
            try {
                List<String> row = row(r.readLine());
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
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public Schema schema() throws IOException {
        return schema; 
    }

    @Override
    public CoordinateReferenceSystem crs() {
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

        Scanner reader = scanner();
        if (opts.hasHeader()) {
            reader.next();
        }

        return new QueryPlan(q).apply(new CSVCursor(reader, this));
    }

    public void close() {
    }
    
    Scanner scanner() throws FileNotFoundException {
        return new Scanner(new BufferedInputStream(new FileInputStream(file))).useDelimiter("\n");
    }

    BufferedReader reader() throws FileNotFoundException {
        return new BufferedReader(new FileReader(file));
    }

    Feature feature(int i, String line) throws IOException {
        List<Object> row = parseRow(line);
        List<Object> values = new ArrayList<Object>();
        values.add(handler.geom(row));
        values.addAll(row);

        return new BasicFeature(String.valueOf(i), values, schema);
    }

    List<String> row(String line) {

        //TODO: this routine isn't very robust, it won't handle escaped quotes

        List<String> row = new ArrayList<String>();

        String[] chunks = line.split("\"");
        for (int i = 0; i < chunks.length; i++) {
            if (i % 2 == 0) {
                String[] split = opts.getDelimiter().split(chunks[i]);
                for (int j = 0; j < split.length; j++) {
                    String str = split[j];
                    if ((j == 0 || j == str.length()-1) && str.isEmpty()) {
                        continue;
                    }
                    
                    row.add(str);
                }
            }
            else {
                row.add(chunks[i]);
            }
        }

        return row;
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
