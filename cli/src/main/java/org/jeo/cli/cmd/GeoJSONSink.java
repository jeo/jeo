package org.jeo.cli.cmd;

import org.jeo.cli.JeoCLI;
import org.jeo.data.Cursor;
import org.jeo.geojson.GeoJSONWriter;
import org.jeo.vector.Feature;
import org.jeo.vector.VectorDataset;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Encodes cursor as GeoJSON to the cli output stream.
 */
public class GeoJSONSink implements VectorSink {
    @Override
    public void encode(Cursor<Feature> cursor, VectorDataset source, JeoCLI cli) throws IOException {
        GeoJSONWriter w = new GeoJSONWriter(cli.console().getOutput());
        w.featureCollection(cursor);
        w.flush();
    }

    @Override
    public String toString() {
        return "geojson";
    }
}
