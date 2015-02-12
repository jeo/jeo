package org.jeo.protobuf;

import java.io.IOException;
import java.util.List;

import org.jeo.data.Cursor;
import org.jeo.vector.BasicFeature;
import org.jeo.vector.Feature;
import org.jeo.vector.Schema;

public class ProtobufAppendCursor extends Cursor<Feature> {

    ProtobufWriter pbw;

    Schema schema;
    Feature next;

    public ProtobufAppendCursor(ProtobufDataset data) throws IOException {
        super(Mode.APPEND);

        pbw = data.writer();
        schema = data.schema();

        pbw.schema(schema);
    }

    @Override
    public boolean hasNext() throws IOException {
        return true;
    }

    @Override
    public Feature next() throws IOException {
        return next = new BasicFeature(null, (List) null, schema);
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
