/* Copyright 2013 The jeo project. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jeo.csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

import com.csvreader.CsvReader;
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
            CsvReader r = reader();
            r.readHeaders();

            try {
                handler.header(r);

                for (String col : r.getHeaders()) {
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
        // TODO: support a .prj file
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

        CsvReader reader = reader();
        if (opts.hasHeader()) {
            reader.readHeaders();
        }

        return new QueryPlan(q).apply(new CSVCursor(reader, this));
    }

    public void close() {
    }

    CsvReader reader() throws FileNotFoundException {
        return new CsvReader(new BufferedReader(new FileReader(file)), opts.getDelimiter());
    }

    Feature feature(int i, CsvReader r) throws IOException {
        List<Object> values = new ArrayList<Object>();
        values.add(handler.geom(r));

        for (String val : r.getValues()) {
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
            values.add(parsed);
        }

        return new BasicFeature(String.valueOf(i), values, schema);
    }
}
