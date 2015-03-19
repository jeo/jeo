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
package org.jeo.geojson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Collections;
import java.util.Map;

import org.jeo.data.Cursor;
import org.jeo.data.Cursor.Mode;
import org.jeo.data.Driver;
import org.jeo.data.FileData;
import org.jeo.vector.FeatureCursor;
import org.jeo.vector.VectorQuery;
import org.jeo.vector.VectorQueryPlan;
import org.jeo.vector.VectorDataset;
import org.jeo.vector.Feature;
import org.jeo.vector.Field;
import org.jeo.vector.Schema;
import org.jeo.vector.SchemaBuilder;
import org.jeo.geojson.parser.CRSFinder;
import org.jeo.geojson.parser.RootHandler;
import org.jeo.json.parser.JSONParser;
import org.jeo.json.parser.ParseException;
import org.jeo.proj.Proj;
import org.jeo.util.Key;
import org.jeo.util.Optional;
import org.jeo.util.Util;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

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
        Optional<Feature> f = first();
        if (f.isPresent()) {
            Schema schema = f.get().schema();
            SchemaBuilder sb = Schema.build(name()).crs(crs());
            for (Field fld : schema) {
                if (fld.geometry() && fld.crs() == null) {
                    sb.field(fld.name(), (Class<Geometry>) fld.type(), crs());
                }
                else {
                    sb.field(fld);
                }
            }
            return sb.schema();
        }
        return null;
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
        return cursor(new VectorQuery()).bounds();
    }

    @Override
    public long count(VectorQuery q) throws IOException {
        return cursor(q).count();
    }

    @Override
    public FeatureCursor cursor(VectorQuery q) throws IOException {
        if (q.mode() == Mode.UPDATE) {
            throw new IOException("Update cursor not supported");
        }
        if (q.mode() == Mode.APPEND) {
            if (!Util.isEmpty(file)) {
                throw new IOException("Can't append to non empty dataset");
            }
            return new GeoJSONAppendCursor(writer());
        }

        return new VectorQueryPlan(q).apply(new GeoJSONCursor(reader()));
    }

    @Override
    public void close() {
    }

    Optional<Feature> first() throws IOException {
        Cursor<Feature> c = cursor(new VectorQuery());
        try {
            if (c.hasNext()) {
                return Optional.of(c.next());
            }
            return Optional.empty();
        }
        finally {
            c.close();
        }
    }

    Reader reader() throws IOException {
        return new BufferedReader(new FileReader(file));
    }

    Writer writer() throws IOException {
        return new BufferedWriter(new FileWriter(file));
    }
}
