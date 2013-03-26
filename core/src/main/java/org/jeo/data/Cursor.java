package org.jeo.data;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

/**
 * An iterator like object used to read contents of {@link Dataset} objects. 
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
public abstract class Cursor<T> implements Closeable, Iterable<T> {

    public static final Mode READ = Mode.READ;
    public static final Mode UPDATE = Mode.UPDATE;
    public static final Mode APPEND = Mode.APPEND;

    public static enum Mode {
        READ, UPDATE, APPEND;
    }

    /**
     * cursor mode
     */
    protected Mode mode;

    protected Cursor() {
        this(Mode.READ);
    }

    protected Cursor(Mode mode) {
        if (mode == null) {
            throw new NullPointerException("mode must not be null");
        }
        this.mode = mode;
    }

    /**
     * The mode of the cursor.
     */
    public Mode getMode() {
        return mode;
    }

    /**
     * Returns <true> if the cursor has more elements.
     */
    public abstract boolean hasNext() throws IOException;

    /**
     * Returns the next object in the cursor.
     * <p>
     * Application code should always check {@link #hasNext()} before calling this method.
     * </p>
     * <p>
     * Implementations should safely return <code>null</code> from this method when the cursor
     * has been exhausted. 
     * </p>
     */
    public abstract T next() throws IOException;

    /**
     * Writes modifications made to the last object returned by the cursor.
     * <p>
     * This method works in {@link Mode#UPDATE} and {@link Mode#APPEND} modes. It will throw 
     * {@link IllegalStateException} in {@link Mode#READ} mode. 
     * </p>
     */
    public void write() throws IOException {
        if (mode == Mode.READ) {
            throw new IllegalStateException("Cursor is read only");
        }

        doWrite();
    }

    protected void doWrite() throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Removes the last object returned by the cursor from the underlying collection.
     * <p>
     * This method works in {@link Mode#UPDATE} mode. It will throw {@link IllegalStateException} 
     * in other modes. 
     * </p>
     */
    public void remove() throws IOException {
        if (mode != Mode.UPDATE) {
            throw new IllegalStateException("Cursor not in update mode");
        }

        doRemove();
    }

    protected void doRemove() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<T> iterator() {
        return Cursors.iterator(this);
    }

}
