package org.jeo.geojson;

import java.io.IOException;
import java.io.Reader;

import org.jeo.data.Cursor;
import org.jeo.feature.Feature;
import org.jeo.geojson.parser.FeatureCollectionHandler;
import org.jeo.geojson.parser.RootHandler;
import org.jeo.geojson.simple.parser.JSONParser;
import org.jeo.geojson.simple.parser.ParseException;

public class GeoJSONCursor extends Cursor<Feature> {

    Reader input;
    JSONParser parser;
    RootHandler handler;

    Feature next;

    GeoJSONCursor(Reader input) {
        this.input = input;
        this.parser = new JSONParser();
        this.handler = new RootHandler(new FeatureCollectionHandler());
    }

    @Override
    public boolean hasNext() throws IOException {
        while (next == null && !handler.isFinished()) {
            try {
                parser.parse(input, handler, true);
                Object obj = handler.getValue();
                if (obj instanceof Feature) {
                    next = (Feature) obj;
                }
            } catch (ParseException e) {
                throw new IOException(e);
            }
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
        if (input != null) {
            input.close();
        }
        input = null;
    }
}
