package org.jeo.data;

import java.io.IOException;

/**
 * A container for data objects.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public interface Registry extends Disposable {

    /**
     * The names of all items of the registry.
     */
    Iterable<DataRef<?>> list() throws IOException;

    /**
     * Returns a registry object by name.
     * 
     * @param name The name of the object.
     * 
     * @return The object or <code>null</code> if so such object matching the name exists.
     */
    Object get(String name) throws IOException;
}
