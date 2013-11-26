package org.jeo.data;

import java.io.IOException;

/**
 * A repository of data objects. 
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public interface DataRepository extends Disposable {

    /**
     * Collection of handles for objects in the repository.
     */
    Iterable<Handle<?>> list() throws IOException;

    /**
     * Returns a object by name.
     *
     * @param name The name of the object.
     * 
     * @return The object or <code>null</code> if so such object matching the name exists.
     */
    Object get(String name) throws IOException;
}
