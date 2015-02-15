/* Copyright 2013 The jeo project. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jeo.data;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import org.jeo.geom.Envelopes;
import org.jeo.util.Consumer;
import org.jeo.util.Function;
import org.jeo.util.Optional;
import org.jeo.util.Predicate;
import org.jeo.vector.Feature;

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
    public Cursor<T> write() throws IOException {
        if (mode == Mode.READ) {
            throw new IllegalStateException("Cursor is read only");
        }

        doWrite();
        return this;
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
    public Cursor<T> remove() throws IOException {
        if (mode != Mode.UPDATE) {
            throw new IllegalStateException("Cursor not in update mode");
        }

        doRemove();
        return this;
    }

    protected void doRemove() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<T> iterator() {
        final Cursor<T> c = this;
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
     * Consumes all the values of the cursor.
     */
    public void forEach(Consumer<T> consumer) throws IOException {
        try (Cursor<T> c = this) {
            for (T obj : c) {
                consumer.accept(obj);
            }
        }
    }

    /**
     * Applies a mapping function to the cursor.
     *
     * @param mapper the mapping function.
     *
     * @return The new cursor.
     */
    public <R> Cursor<R> map(final Function<T,R> mapper) throws IOException {
        return new CursorWrapper<R>(this) {
            @Override
            public R next() throws IOException {
                return mapper.apply((T)delegate.next());
            }
        };
    }

    /**
     * Returns the first element of a cursor, returning empty if the cursor has no more objects.
     */
    public Optional<T> first() throws IOException {
        if (hasNext()) {
            return Optional.of(next());
        }

        return Optional.empty();
    }

    /**
     * Returns the number of results in the cursor.
     */
    public long count() throws IOException {
        try {
            int count = 0;
            while(hasNext()) {
                next();
                count++;
            }
            return count;
        }
        finally {
            close();
        }
    }

    /**
     * Returns the aggregated spatial extent of results in the cursor.
     * <p>
     * This method works on cursors containing {@link Feature} or {@link Geometry} objects.
     * Use {@link #bounds(org.jeo.util.Function)} to provide an explicit bounds provider.
     * </p>
     *
     * TODO: move this to FeatureCursor
     */
    public Envelope bounds() throws IOException {
        return bounds(new Function<T, Envelope>() {
            @Override
            public Envelope apply(T obj) {
                Geometry g = null;
                if (obj instanceof Geometry) {
                    g = (Geometry) obj;
                } else if (obj instanceof Feature) {
                    g = ((Feature) obj).geometry();
                }
                if (g != null) {
                    return g.getEnvelopeInternal();
                }
                return null;
            }
        });
    }

    /**
     * Returns the aggregated spatial extent of results in the cursor.
     *
     * @param env Function providing envelopes for objects in the cursor.
     */
    public Envelope bounds(Function<T, Envelope> env) throws IOException {
        try {
            Envelope extent = new Envelope();
            extent.setToNull();

            for (T obj : this) {
                Envelope e = env.apply(obj);
                if (!Envelopes.isNull(e)) {
                    extent.expandToInclude(e);
                }
            }
            return extent;
        }
        finally {
            close();
        }
    }

    /**
     * Limits the number of results given back by the cursor to a fixed size.
     *
     * @param limit The maximum number of objects to return.
     *
     * @return The limited cursor.
     */
    public Cursor<T> limit(Integer limit) {
        return new LimitCursor<T>(this, limit);
    }

    static class LimitCursor<T> extends CursorWrapper<T> {

        Integer limit;
        Integer count;

        LimitCursor(Cursor<T> delegate, Integer limit) {
            super(delegate);

            if (limit == null) {
                throw new NullPointerException("limit must not be null");
            }

            this.limit = limit;
            this.count = 0;
        }

        @Override
        public boolean hasNext() throws IOException {
            if (count < limit) {
                return delegate.hasNext();
            }
            return false;
        }

        @Override
        public T next() throws IOException {
            count++;
            return delegate.next();
        }
    }

    /**
     * Returns a cursor that will skip over the specified number of objects.
     *
     * @param offset The number of objects to skip over.
     *
     * @return The skipped cursor.
     */
    public Cursor<T> skip(Integer offset) {
        return new OffsetCursor<T>(this, offset);
    }

    static class OffsetCursor<T> extends CursorWrapper<T> {

        Integer offset;

        OffsetCursor(Cursor<T> delegate, Integer offset) {
            super(delegate);

            if (offset == null) {
                throw new NullPointerException("limit must not be null");
            }

            this.offset = offset;
        }

        @Override
        public boolean hasNext() throws IOException {
            if (offset != null) {
                for (int i = 0; i < offset && delegate.hasNext(); i++) {
                    delegate.next();
                }
                offset = null;
            }
            return delegate.hasNext();
        }
    }

    /**
     * Wraps a cursor returning objects that pass a predicate.
     *
     * @param filter The predicate used to filter objects.
     *
     * @return The filtered cursor.
     */
    public Cursor<T> filter(Predicate<T> filter) {
        return new FilterCursor<T>(this, filter);
    }

    private static class FilterCursor<T> extends CursorWrapper<T> {

        Predicate<T> filter;
        T next;

        FilterCursor(Cursor<T> delegate, Predicate<T> filter) {
            super(delegate);
            this.filter = filter;
        }

        @Override
        public boolean hasNext() throws IOException {
            while(delegate.hasNext() && next == null) {
                T obj = delegate.next();
                if (filter.test(obj)) {
                    next = obj;
                }
            }
            return next != null;
        }

        @Override
        public T next() throws IOException {
            T obj = next;
            next = null;
            return obj;
        }
    }

    static class CursorWrapper<T> extends Cursor<T> {
        protected Cursor<T> delegate;

        CursorWrapper(Cursor delegate) {
            this.delegate = delegate;
        }

        @Override
        public boolean hasNext() throws IOException {
            return delegate.hasNext();
        }

        @Override
        public T next() throws IOException {
            return delegate.next();
        }

        @Override
        public Cursor<T> write() throws IOException {
            return delegate.write();
        }

        @Override
        public Cursor<T> remove() throws IOException {
            return delegate.remove();
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }
    }

}
