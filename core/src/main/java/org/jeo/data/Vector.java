package org.jeo.data;

import java.io.IOException;

import org.jeo.feature.Feature;
import org.jeo.feature.Schema;

import com.vividsolutions.jts.geom.Envelope;

public interface Vector extends Layer {

    /**
     * The schema for the layer.
     */
    Schema getSchema() throws IOException;

    /**
     * Counts features in the layer.
    */
    long count(Envelope bbox) throws IOException ;

    /**
     * Reads data from the layer. 
     */
    Cursor<Feature> read(Envelope bbox) throws IOException;

    /**
     * Adds a feature to the layer.
     */
    void add(Feature f) throws IOException;
}
