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
package io.jeo.geojson;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Map;

import io.jeo.data.Driver;
import io.jeo.data.FileData;
import io.jeo.geojson.parser.CRSFinder;
import io.jeo.json.parser.ParseException;
import io.jeo.util.Util;
import io.jeo.vector.FeatureAppendCursor;
import io.jeo.vector.FeatureCursor;
import io.jeo.vector.FeatureWriteCursor;
import io.jeo.vector.Features;
import io.jeo.vector.VectorQueryPlan;
import io.jeo.geojson.parser.RootHandler;
import io.jeo.json.parser.JSONParser;
import io.jeo.proj.Proj;
import io.jeo.util.Key;
import io.jeo.vector.Schema;
import io.jeo.vector.VectorDataset;
import io.jeo.vector.VectorQuery;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

public class GeoJSONDataset implements VectorDataset, FileData {

    File file;

    public GeoJSONDataset(File file) {
        this.file = file;
    }

    @Override
    public Driver<?> driver() {
        return new GeoJSON();
    }

    @Override
    public Map<Key<?>, Object> driverOptions() {
        return (Map) Collections.singletonMap(GeoJSON.FILE, file);
    }

    @Override
    public File file() {
        return file;
    }

    @Override
    public String name() {
        return Util.base(file.getName());
    }

    @Override
    public Schema schema() throws IOException {
        return Features.schema(this).orElse(null);
    }

    @Override
    public CoordinateReferenceSystem crs() throws IOException {
        //TODO: probably should cache this

        CoordinateReferenceSystem crs = null;

        //first scan for a crs property
        JSONParser p = new JSONParser();
        Reader r = reader();
        try {
            CRSFinder f = new CRSFinder();
            try {
                p.parse(r, new RootHandler(f), true);
                crs = f.getCRS();
            } catch (ParseException e) {
                throw new IOException(e);
            }
        }
        finally {
            r.close();
        }

        // GeoJSON actually specified that the data should be 4326 so fall back on that
        return crs != null ? crs : Proj.EPSG_4326;
    }

    @Override
    public Envelope bounds() throws IOException {
        return read(new VectorQuery()).bounds();
    }

    @Override
    public long count(VectorQuery q) throws IOException {
        return read(q).count();
    }

    @Override
    public FeatureCursor read(VectorQuery q) throws IOException {
        return new VectorQueryPlan(q).apply(new GeoJSONCursor(reader()));
    }

    @Override
    public FeatureWriteCursor update(VectorQuery q) throws IOException {
        throw new IOException("Update cursor not supported");
    }

    @Override
    public FeatureAppendCursor append(VectorQuery q) throws IOException {
        if (!Util.isEmpty(file)) {
            throw new IOException("Can't append to non empty dataset");
        }
        return new GeoJSONAppendCursor(writer());
    }

    @Override
    public void close() {
    }

    Reader reader() throws IOException {
        return Files.newBufferedReader(file.toPath(), Charset.forName("UTF8"));
    }

    Writer writer() throws IOException {
        return Files.newBufferedWriter(file.toPath(), Charset.forName("UTF8"));
    }
}
