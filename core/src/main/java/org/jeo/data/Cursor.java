package org.jeo.data;

import java.io.Closeable;
import java.io.IOException;

/**
 * An iterator like object used to read contents of {@link Layer} objects. 
 * <p>
 * Example usage:
 * <pre>
 *  Cursor<Object> c = getCursor()
 *  try {
 *    while(c.hasNext()) {
 *      Object next = c.next();
 *      doSomethingWith(next);
 *    }
 *  }
 *  finally {
 *    c.close();
 *  }
 * </pre>
 * </p>
 * <p>
 * Since Cursor implements {@link Iterable} it can be used with for-each loops:
 * <pre>
 * for (Object next : getCursor()) {
 *   doSomethingWith(next);
 * }
 * </pre>
 * When used in this form the {@link #close()} method need not be called.
 * </p>
 * <p>
 * Applications must always call the {link #close()} method when the cursor is no longer needed, 
 * except when using the cursor in a for-each loop unless the for-each loop is exited prematurely.
 * </p>
 * <p>
 * Implementors should ensure that the iterator returned from {@link #iterator()} closes itself
 * when the iterator is exhausted. Implementors should also ensure that the {@link #close()} method
 * can be called multiple times safely.   
 * </p>
 */
public interface Cursor<T> extends Closeable, Iterable<T> {

    /**
     * Returns <true> if the cursor has more elements.
     */
    boolean hasNext() throws IOException;

    /**
     * Returns the next object in the cursor.
     * <p>
     * Application code should always check {@link #hasNext()} before calling this method.
     * </p>
     * <p>
     * Implementations should safely return <code>null</code> from this method when the curssor
     * has been exhaused. 
     * </p>
     */
    T next() throws IOException;
}
