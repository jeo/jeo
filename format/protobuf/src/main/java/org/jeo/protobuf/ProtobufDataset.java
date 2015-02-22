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
package org.jeo.protobuf;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.jeo.data.Driver;
import org.jeo.data.FileData;
import org.jeo.data.Cursor.Mode;
import org.jeo.vector.FeatureCursor;
import org.jeo.vector.VectorQuery;
import org.jeo.vector.VectorQueryPlan;
import org.jeo.vector.VectorDataset;
import org.jeo.vector.Schema;
import org.jeo.util.Key;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

public class ProtobufDataset implements VectorDataset, FileData {

    File file;
    Schema schema;

    public ProtobufDataset(File file) throws IOException {
        this.file = file;
        this.schema = readSchema();
    }

    public ProtobufDataset(File file, Schema schema) throws IOException {
        this.file = file;
        this.schema = schema;
    }

    @Override
    public Schema schema() throws IOException {
        return schema;
    }

    Schema readSchema() throws IOException {
        ProtobufReader reader = reader();
        try {
            return reader.schema();
        }
        finally {
            reader.close();
        }
    }

    @Override
    public Driver<?> driver() {
        return new Protobuf();
    }

    @Override
    public Map<Key<?>, Object> driverOptions() {
        return (Map) Collections.singletonMap(Protobuf.FILE, file);
    }

    @Override
    public File file() {
        return file;
    }

    @Override
    public String name() {
        return schema.getName();
    }

    @Override
    public String title() {
        return null;
    }

    @Override
    public String description() {
        return null;
    }

    @Override
    public CoordinateReferenceSystem crs() throws IOException {
        return schema.crs();
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
            if (!fileIsEmpty()) {
                throw new IOException("Can't append to non empty dataset");
            }
            return new ProtobufAppendCursor(this);
        }

        return new VectorQueryPlan(q).apply(new ProtobufCursor(this));
    }

    @Override
    public void close() {
    }

    ProtobufReader reader() throws IOException {
        return new ProtobufReader(new BufferedInputStream(new FileInputStream(file)));
    }

    ProtobufWriter writer() throws IOException {
        return new ProtobufWriter(new BufferedOutputStream(new FileOutputStream(file)));
    }

    boolean fileIsEmpty() {
        return !file.exists() || file.length() == 0;
    }
}
