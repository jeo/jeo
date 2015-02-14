package org.jeo.cli.cmd;

import org.jeo.cli.JeoCLI;
import org.jeo.data.Cursor;
import org.jeo.protobuf.ProtobufWriter;
import org.jeo.util.Supplier;
import org.jeo.vector.Feature;
import org.jeo.vector.Schema;
import org.jeo.vector.VectorDataset;

import java.io.IOException;

/**
 * Encodes cursor as a protobuf to the cli output stream.
 */
public class ProtobufSink implements VectorSink {
    @Override
    public void encode(Cursor<Feature> cursor, VectorDataset source, JeoCLI cli) throws IOException {
        try (ProtobufWriter writer = new ProtobufWriter(cli.output())) {
            cursor = cursor.buffer(1);
            Schema schema = cursor.first().orElseThrow(new Supplier<RuntimeException>() {
                @Override
                public RuntimeException get() {
                    return new IllegalArgumentException("No data to write");
                }
            }).schema();

            writer.schema(schema);
            cursor.rewind();

            while (cursor.hasNext()) {
                writer.feature(cursor.next());
            }
        }
    }

    @Override
    public String toString() {
        return "pbf";
    }
}
