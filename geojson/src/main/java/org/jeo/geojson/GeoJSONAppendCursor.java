package org.jeo.geojson;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;

import org.jeo.data.Cursor;
import org.jeo.feature.Feature;
import org.jeo.feature.MapFeature;

import com.vividsolutions.jts.geom.Geometry;

public class GeoJSONAppendCursor extends Cursor<Feature> {

    FeatureCollectionWriter writer;
    Feature next;

    public GeoJSONAppendCursor(Writer out) throws IOException {
        super(Mode.APPEND);
        writer = new FeatureCollectionWriter(out);
        writer.start();
    }

    @Override
    public boolean hasNext() throws IOException {
        return true;
    }
    
    @Override
    public Feature next() throws IOException {
        return next = new MapFeature(null, new HashMap<String, Object>()) {
            @Override
            public void put(Geometry g) {
                //hack
                put("geometry", g);
            }
        };
    }

    @Override
    protected void doWrite() throws IOException {
        writer.feature(next);
    }

    @Override
    public void close() throws IOException {
        if (writer != null) {
            writer.end();
        }
        writer = null;
    }

    class FeatureCollectionWriter {
    
        GeoJSONWriter writer;
        boolean first = true;

        public FeatureCollectionWriter(Writer out) {
            writer = new GeoJSONWriter(out);
        }

        void start() throws IOException {
            Writer out = writer.getWriter();

            out.write("{");
            out.write("\"type\": \"FeatureCollection\", ");
            out.write("\"features\": [");

            out.flush();
        }

        void feature(Feature f) throws IOException {
            Writer out = writer.getWriter();

            if (!first) {
                out.write(",");
            }
            first = false;
            writer.feature(f);
            out.flush();
        }

        void end() throws IOException {
            Writer out = writer.getWriter();
            out.write("]}");
            out.flush();
            out.close();
        }

    }
}
