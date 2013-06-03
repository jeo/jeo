package org.jeo.data;

import java.io.Closeable;
import java.io.IOException;

/**
 * Interface for objects that should be closed after usage.
 * <p>
 * Such objects typically hold onto resources such as file handles, database connections, etc..
 * </p>
 * <p>
 * This interface extends from {@link Closeable} but overrides the {@link #close()} to ensure
 *  no IOException is thrown. 
 * </p>
 * @author Justin Deoliveira, OpenGeo
 *
 */
public interface Disposable extends Closeable {

    /**
     * Disposes the object. 
     * <p>
     * Unlike {@link Closeable#close()} this method does not throw {@link IOException} and 
     * implementations must ensure no exceptions are thrown from this method.
     * </p>
     * <p>
     * Application code must always be sure to call this method on any implementing class. 
     * Implementing classes should handle multiple calls to this method.  
     * </p>
     */
    void close();
}
