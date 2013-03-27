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
     * @param q Query used to constrain results, must not be <code>null</code>
    */
    long count(Query q) throws IOException ;

    /**
     * Returns a feature cursor for the layer. 
     * <p>
     * {@link Query#getMode()} is used to control whether the cursor is read or write. All 
     * implementations must support {@link Cursor.Mode#READ}.
     * </p>
     * @param q A query used to constrain results, must not be <code>null</code>.
     */
    Cursor<Feature> cursor(Query q) throws IOException;

}
