package org.jeo.protobuf;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.jeo.data.FileDriver;
import org.jeo.util.Messages;
import org.jeo.vector.VectorDriver;
import org.jeo.vector.Schema;

/**
 * Google Protocol Buffer (Protobuf) driver.
 * <p>
 * Usage:
 * <pre><code>
 * Protobuf.open(new File("states.pbf"));
 * </code></pre>
 * </p>
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class Protobuf extends FileDriver<ProtobufDataset> implements VectorDriver<ProtobufDataset> {

    public static ProtobufDataset open(File file) throws IOException {
        return new ProtobufDataset(file);
    }

    @Override
    public String getName() {
        return "Protobuf";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("pb", "pbf");
    }

    @Override
    public Class<ProtobufDataset> getType() {
        return ProtobufDataset.class;
    }
    
    @Override
    public ProtobufDataset open(File file, Map opts) throws IOException {
        return open(file);
    }

    @Override
    public boolean supports(Capability cap) {
        return false;
    }

    @Override
    public boolean canCreate(Map<?, Object> opts, Messages msgs) {
        return FILE.has(opts);
    }

    @Override
    public ProtobufDataset create(Map<?, Object> opts, Schema schema) throws IOException {
        return new ProtobufDataset(FILE.get(opts), schema);
    }
}
