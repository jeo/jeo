package org.jeo.cli.conv;

import com.beust.jcommander.IStringConverter;
import org.jeo.cli.cmd.GeoJSONSink;
import org.jeo.cli.cmd.JeoCmd;
import org.jeo.cli.cmd.ProtobufSink;
import org.jeo.cli.cmd.VectorSink;
import org.jeo.cli.cmd.WorkspaceSink;
import org.jeo.util.Pair;
import org.neo4j.kernel.impl.transaction.IllegalResourceException;

import java.net.URI;

public class VectorSinkConverter implements IStringConverter<VectorSink> {
    @Override
    public VectorSink convert(String str) {
        if ("pbf".equalsIgnoreCase(str)) {
            return new ProtobufSink();
        }
        if ("geojson".equalsIgnoreCase(str) || "json".equalsIgnoreCase(str)) {
            return new GeoJSONSink();
        }

        try {
            return new WorkspaceSink(JeoCmd.parseDataURI(str));
        }
        catch(IllegalArgumentException e) {
        }

        throw new IllegalArgumentException("Unrecognized output: " + str);
    }
}
