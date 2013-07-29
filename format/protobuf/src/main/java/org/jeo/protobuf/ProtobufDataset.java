package org.jeo.protobuf;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.jeo.data.Cursor;
import org.jeo.data.Cursors;
import org.jeo.data.Driver;
import org.jeo.data.FileData;
import org.jeo.data.Query;
import org.jeo.data.QueryPlan;
import org.jeo.data.VectorData;
import org.jeo.data.Cursor.Mode;
import org.jeo.feature.Feature;
import org.jeo.feature.Schema;
import org.jeo.util.Key;
import org.jeo.util.Util;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

public class ProtobufDataset implements VectorData, FileData {

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
    public Driver<?> getDriver() {
        return new Protobuf();
    }

    @Override
    public Map<Key<?>, Object> getDriverOptions() {
        return (Map) Collections.singletonMap(Protobuf.FILE, file);
    }

    @Override
    public File getFile() {
        return file;
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
    public Schema getSchema() throws IOException {
        return schema;
    }

    @Override
    public CoordinateReferenceSystem getCRS() throws IOException {
        return schema.crs();
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
        if (q.getMode() == Mode.UPDATE) {
            throw new IOException("Update cursor not supported");
        }
        if (q.getMode() == Mode.APPEND) {
            if (!fileIsEmpty()) {
                throw new IOException("Can't append to non empty dataset");
            }
            return new ProtobufAppendCursor(this);
        }

        return new QueryPlan(q).apply(new ProtobufCursor(this));
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
