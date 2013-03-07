package org.jeo.data;

/**
 * Interface for objects that should be disposed after usage.
 * <p>
 * Such objects typically hold onto resouces such as file handles, database connectionions, etc..
 * </p>
 * @author Justin Deoliveira, OpenGeo
 *
 */
public interface Disposable {

    /**
     * Disposes the object. 
     * <p>
     * Application code must always be sure to call this method on any implementing class. 
     * Implementing classes should handle multiple calls to this method.  
     * </p>
     */
    void dispose();
}
