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
package io.jeo.csv;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.jeo.data.FileData;
import io.jeo.geom.Bounds;
import io.jeo.util.Key;
import io.jeo.util.Util;
import io.jeo.vector.Feature;
import io.jeo.vector.FeatureAppendCursor;
import io.jeo.vector.FeatureCursor;
import io.jeo.vector.FeatureWriteCursor;
import io.jeo.vector.ListFeature;
import io.jeo.vector.Schema;
import io.jeo.vector.SchemaBuilder;
import io.jeo.vector.VectorDataset;
import io.jeo.vector.VectorQuery;
import io.jeo.vector.VectorQueryPlan;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.csvreader.CsvReader;
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
    public CSV driver() {
        return new CSV();
    }

    @Override
    public Map<Key<?>, Object> driverOptions() {
        return opts.toMap();
    }

    @Override
    public File file() {
        return file;
    }

    void init() throws IOException {
        SchemaBuilder sb = Schema.build(name());
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
    public String name() {
        return Util.base(file.getName());
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
    public Bounds bounds() throws IOException {
        return read(new VectorQuery()).bounds();
    }

    @Override
    public long count(VectorQuery q) throws IOException {
        return read(q).count();
    }

    @Override
    public FeatureCursor read(VectorQuery q) throws IOException {
        CsvReader reader = reader();
        if (opts.hasHeader()) {
            reader.readHeaders();
        }

        return new VectorQueryPlan(q).apply(new CSVCursor(reader, this));
    }

    @Override
    public FeatureWriteCursor update(VectorQuery q) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public FeatureAppendCursor append(VectorQuery q) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void close() {
    }

    CsvReader reader() throws IOException {
        return new CsvReader(Files.newBufferedReader(file.toPath(), Util.UTF_8), opts.getDelimiter());
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

        return new ListFeature(String.valueOf(i), schema, values);
    }
}
