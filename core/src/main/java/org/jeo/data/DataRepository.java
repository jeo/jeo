package org.jeo.data;

import java.io.IOException;

import org.jeo.filter.Filter;

/**
 * A repository of data objects. A repository can contain Workspaces and Style
 * objects. For any type of object, the name must be unique in the
 * DataRepository.
 *
 * @author Justin Deoliveira, OpenGeo
 */
public interface DataRepository extends Disposable {

    /**
     * Queries handles present in the repository.
     * @param filter The non-null filter to use for querying
     *
     * @return An Iterable of Handle objects that match the filter
     */
    Iterable<Handle<?>> query(Filter<? super Handle<?>> filter) throws IOException;

    /**
     * Returns a data object object by name.
     *
     * @param name The non-null name of the object.
     * @param type The non-null type of the object, Workspace or Style supported
     * 
     * @return The object or <code>null</code> if so such object matching the name and type exists.
     */
    <T> T get(String name, Class<T> type) throws IOException;

}
