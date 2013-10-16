package org.jeo.geojson;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;

import org.jeo.data.Cursor;
import org.jeo.feature.BasicFeature;
import org.jeo.feature.Feature;

import com.vividsolutions.jts.geom.Geometry;

public class GeoJSONAppendCursor extends Cursor<Feature> {

    GeoJSONWriter writer;
    Feature next;

    public GeoJSONAppendCursor(Writer out) throws IOException {
        super(Mode.APPEND);
        writer = new GeoJSONWriter(out);
        writer.featureCollection();
    }

    @Override
    public boolean hasNext() throws IOException {
        return true;
    }
    
    @Override
    public Feature next() throws IOException {
        return next = new BasicFeature(null, new HashMap<String, Object>()) {
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
            writer.endFeatureCollection();
            writer.flush();
        }
        writer = null;
    }
}
