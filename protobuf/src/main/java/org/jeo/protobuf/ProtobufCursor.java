package org.jeo.protobuf;

import java.io.IOException;

import org.jeo.data.Cursor;
import org.jeo.vector.Feature;
import org.jeo.vector.Schema;

public class ProtobufCursor extends Cursor<Feature> {

    ProtobufReader pbr;
    Schema schema;
    Feature next;

    public ProtobufCursor(ProtobufDataset data) throws IOException {
        pbr = data.reader();

        // skip over the schema
        schema = pbr.schema();
    }

    @Override
    public boolean hasNext() throws IOException {
        if (next == null) {
            next = pbr.feature(schema);
        }
        return next != null;
    }

    @Override
    public Feature next() throws IOException {
        try {
            return next;
        }
        finally {
            next = null;
        }
    }

    @Override
    public void close() throws IOException {
        if (pbr != null) {
            pbr.close();
        }
        pbr = null;
    }
}
