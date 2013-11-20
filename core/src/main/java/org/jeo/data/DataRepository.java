package org.jeo.data;

import java.io.IOException;

import org.jeo.filter.Filter;

/**
 * A repository of workspace objects. 
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public interface DataRepository extends Disposable {

    /**
     * Queries handles present in the repository.
     */
    Iterable<Handle<Object>> query(Filter<? super Handle<Object>> filter) throws IOException;

    /**
     * Returns a data object object by name.
     *
     * @param name The name of the object.
     * 
     * @return The object or <code>null</code> if so such object matching the name exists.
     */
    Object get(String name) throws IOException;

}
