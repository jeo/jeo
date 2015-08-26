/* Copyright 2015 The jeo project. All rights reserved.
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
package io.jeo.geobuf;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Map;

import io.jeo.data.Driver;
import io.jeo.data.FileData;
import io.jeo.geobuf.Geobuf.Data.DataTypeCase;
import io.jeo.geom.Bounds;
import io.jeo.util.Util;
import io.jeo.vector.FeatureAppendCursor;
import io.jeo.vector.FeatureCursor;
import io.jeo.vector.FeatureWriteCursor;
import io.jeo.vector.Features;
import io.jeo.vector.VectorQuery;
import io.jeo.vector.VectorDataset;
import io.jeo.vector.Schema;
import io.jeo.util.Key;
import io.jeo.vector.VectorQueryPlan;
import org.osgeo.proj4j.CoordinateReferenceSystem;

public class GeobufDataset implements VectorDataset, FileData {

    File file;
    GeobufReader reader;

    public GeobufDataset(File file) throws IOException {
        this.file = file;
    }

    @Override
    public Schema schema() throws IOException {
        return Features.schema(this).orElse(null);
    }

    @Override
    public Driver<?> driver() {
        return new Gbf();
    }

    @Override
    public Map<Key<?>, Object> driverOptions() {
        return (Map) Collections.singletonMap(Gbf.FILE, file);
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
    public CoordinateReferenceSystem crs() throws IOException {
        return reader().crs();
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
        return new VectorQueryPlan(q).apply(reader().featureCollection());
    }

    @Override
    public FeatureWriteCursor update(VectorQuery q) throws IOException {
        throw new IOException("Update cursor not supported");
    }

    @Override
    public FeatureAppendCursor append(VectorQuery q) throws IOException {
        if (!fileIsEmpty()) {
            throw new IOException("Can't append to non empty dataset");
        }
        return new GeobufAppendCursor(this);
    }

    @Override
    public void close() {
        if (reader != null) {
            reader.close();
            reader = null;
        }
    }

    GeobufReader reader() throws IOException {
        if (reader == null) {
            reader = new GeobufReader(Files.newInputStream(file.toPath()));
            if (reader.data.getDataTypeCase() != DataTypeCase.FEATURE_COLLECTION) {
                // TODO: wrap geometry and feature?
                throw new IOException("Geobuf not a feature collection");
            }
        }
        return reader;
    }

    GeobufWriter writer() throws IOException {
        return new GeobufWriter(Files.newOutputStream(file.toPath()));
    }

    boolean fileIsEmpty() {
        return !file.exists() || file.length() == 0;
    }
}
