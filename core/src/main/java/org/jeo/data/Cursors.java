package org.jeo.data;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

/**
 * Utility class for {@link Cursor} objects.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class Cursors {

    /**
     * Returns an {@link Iterator} for a cursor object.
     * <p>
     * This method should be typically used by cursor implementors implementing the {@link Iterable} 
     * interface.
     * </p>
     */
    public static <T> Iterator<T> iterator(final Cursor<T> c) {
        return new Iterator<T>() {
            boolean closed = false;

            @Override
            public boolean hasNext() {
                if (closed) {
                    return false;
                }

                try {
                    boolean hasNext = c.hasNext();
                    if (!hasNext) {
                        //close the cursor
                        c.close();
                    }

                    return hasNext;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public T next() {
                try {
                    return c.next();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * Returns a typed empty cursor object.
     */
    public static <T> Cursor<T> empty(Class<T> clazz) {
        return new Cursor<T>() {
            @Override
            public boolean hasNext() throws IOException {
                return false;
            }
            @Override
            public T next() throws IOException {
                return null;
            }
            @Override
            public void close() throws IOException {
            }

            @Override
            public Iterator<T> iterator() {
                return Cursors.iterator(this);
            }
        };
    }

    /**
     * Returns a cursor containing a single object.
     */
    public static <T> Cursor<T> single(T obj) {
        return new SingleCursor<T>(obj);
    }

    private static class SingleCursor<T> implements Cursor<T> {

        T obj;

        SingleCursor(T obj) {
            this.obj = obj;
        }

        @Override
        public boolean hasNext() throws IOException {
            return obj != null;
        }

        @Override
        public T next() throws IOException {
            try {
                return obj;
            }
            finally {
                obj = null;
            }
        }

        @Override
        public void close() throws IOException {
        }

        @Override
        public Iterator<T> iterator() {
            return Cursors.iterator(this);
        }
    }

    /**
     * Creates a cursor from an existing collection.
     */
    public static <T> Cursor<T> create(Collection<T> collection) {
        return create(collection.iterator());
    }

    /**
     * Creates a cursor from an existing iterator.
     */
    public static <T> Cursor<T> create(Iterator<T> it) {
        return new IteratorCursor<T>(it);
    }

    private static class IteratorCursor<T> implements Cursor<T> {
        Iterator<T> it;

        IteratorCursor(Iterator<T> it) {
            this.it = it;
        }

        @Override
        public Iterator<T> iterator() {
            return it;
        }

        @Override
        public boolean hasNext() throws IOException {
            return it.hasNext();
        }

        @Override
        public T next() throws IOException {
            return it.next();
        }

        @Override
        public void close() throws IOException {
            // TODO Auto-generated method stub
        }
    }
}

