package org.jeo.data;

import java.io.IOException;

import org.jeo.feature.Feature;
import org.jeo.feature.Schema;

import com.vividsolutions.jts.geom.Envelope;

/**
 * A layer consisting of vector geometry objects, or {@link Feature} objects. 
 *
 * @author Justin Deoliveira, OpenGeo
 */
public interface Vector extends Dataset {

    /**
     * The schema for the layer.
     */
    Schema getSchema() throws IOException;

    /**
     * Counts features in the layer.
     * 
     * @param bbox A bounding box filter used to constrain results, or <code>null</code> to specify
     * no constraint.
    */
    long count(Envelope bbox) throws IOException ;

    /**
     * Reads data from the layer. 
     * 
     * @param bbox A bounding box filter used to constrain results, or <code>null</code> to specify
     * no constraint.
     */
    Cursor<Feature> read(Envelope bbox) throws IOException;

    /**
     * Adds a feature to the layer.
     */
    void add(Feature f) throws IOException;
}
