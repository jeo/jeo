package org.jeo.cli.cmd;

import org.jeo.cli.JeoCLI;
import org.jeo.data.Cursor;
import org.jeo.data.Dataset;
import org.jeo.protobuf.ProtobufCursor;
import org.jeo.protobuf.ProtobufReader;
import org.jeo.util.Function;
import org.jeo.util.Optional;
import org.jeo.util.Predicate;
import org.jeo.vector.Feature;
import org.jeo.vector.FeatureCursor;
import org.jeo.vector.VectorDataset;
import org.jeo.vector.VectorQuery;
import org.jeo.vector.VectorQueryPlan;

import java.io.IOException;

/**
 * Base class for commands that work on vector data.
 */
public abstract class VectorCmd extends JeoCmd {

    /**
     * Opens a vector dataset from a dataset uri.
     */
    protected Optional<VectorDataset> openVectorDataset(String ref) throws IOException {
        Optional<Dataset> data = openDataset(ref);
        if (data.isPresent() && !(data.get() instanceof VectorDataset)) {
            throw new IllegalArgumentException(ref + " is not a vector dataset");
        }

        return data.map(new Function<Dataset, VectorDataset>() {
            @Override
            public VectorDataset apply(Dataset value) {
                return (VectorDataset) value;
            }
        });
    }

    /**
     * Obtains an input cursor from stdin.
     */
    protected FeatureCursor cursorFromStdin(JeoCLI cli) throws IOException {
        // look for input from stdin
        // TODO: something better than just assuming pbf
        return new ProtobufCursor(new ProtobufReader(cli.console().getInput()).setReadUntilLastFeature());
    }
}
