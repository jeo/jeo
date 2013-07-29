package org.jeo.protobuf;

import java.io.IOException;

import org.jeo.data.Cursor;
import org.jeo.feature.Feature;
import org.jeo.feature.ListFeature;
import org.jeo.feature.Schema;

public class ProtobufAppendCursor extends Cursor<Feature> {

    ProtobufWriter pbw;

    Schema schema;
    Feature next;

    public ProtobufAppendCursor(ProtobufDataset data) throws IOException {
        super(Mode.APPEND);

        pbw = data.writer();
        schema = data.getSchema();

        pbw.schema(schema);
    }

    @Override
    public boolean hasNext() throws IOException {
        return true;
    }

    @Override
    public Feature next() throws IOException {
        return next = new ListFeature(null, null, schema);
        /*return next = new MapFeature(null, new HashMap<String, Object>()) {
            @Override
            public void put(Geometry g) {
                //hack
                put("geometry", g);
            }
        };*/
    }

    @Override
    protected void doWrite() throws IOException {
        pbw.feature(next);
    }

    @Override
    public void close() throws IOException {
        if (pbw != null) {
            pbw.close();
        }
        pbw = null;
    }
}
