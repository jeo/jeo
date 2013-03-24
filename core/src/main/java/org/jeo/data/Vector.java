package org.jeo.data;

import java.io.IOException;

import org.jeo.feature.Feature;
import org.jeo.feature.Schema;

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
     * @param q Query used to constrain results, or <code>null</code> to specify no constraint 
     * and count all features.
    */
    long count(Query q) throws IOException ;

    /**
     * Determines if the vector dataset supports the specified cursor mode.
     */
    boolean supports(Cursor.Mode mode);

    /**
     * Returns a feature cursor for the layer. 
     * <p>
     * The <tt>mode</tt> is used to control whether the cursor is read or write. All implementations
     * must support {@link Cursor.Mode#READ}. The {@link #supports(org.jeo.data.Cursor.Mode)} method
     * is used to determine if other modes are supported.
     * </p>
     * @param q A query used to constrain results, or <code>null</code> to specify no constraint and 
     * query all features.
     * @param mode The cursor mode, <code>null</code> is interpreted as {@link Cursor.Mode#READ}. 
     */
    Cursor<Feature> cursor(Query q, Cursor.Mode mode) throws IOException;

    /**
     * Adds a feature to the layer.
     */
    void add(Feature f) throws IOException;
}
