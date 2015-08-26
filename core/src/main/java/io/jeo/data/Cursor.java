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
package io.jeo.data;

import com.vividsolutions.jts.geom.Geometry;
import io.jeo.geom.Bounds;
import io.jeo.util.Consumer;
import io.jeo.util.Function;
import io.jeo.util.Optional;
import io.jeo.util.Predicate;
import io.jeo.vector.Feature;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

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
 * However unless the cursor supports being {@link #rewind()} it is a one-time pass through the data so can only be used
 * for a single loop.
 * </p>
 * <p>
 * When used in for-each form the {@link #close()} method will be automatically called when the iterator has been
 * exhausted. However if the loop exists prematurely it will not be so to be safe the loop should be wrapped in a try
 * finally close block.
 * </p>
 *
 */
public abstract class Cursor<T> implements Closeable, Iterable<T> {

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
     * Returns an iterator view of the cursor.
     * <p>
     * This iterator implementation closes the cursor upon exhaustion.
     * </p>
     */
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
     * Consumes all the values of the stream.
     */
    public void each(Consumer<T> consumer) throws IOException {
        try (Cursor<T> c = this) {
            while (c.hasNext()) {
                consumer.accept(c.next());
            }
        }
    }

    /**
     * Returns the next element of the stream, returning empty if the stream has no more objects.
     */
    public Optional<T> first() throws IOException {
        if (hasNext()) {
            return Optional.of(next());
        }

        return Optional.empty();
    }

    /**
     * Returns the number of results in the stream.
     */
    public long count() throws IOException {
        long count = 0;
        try (Cursor<T> c = this) {
            while (c.hasNext()) {
                count++;
                c.next();
            }
        }

        return count;
    }

    /**
     * Returns the aggregated spatial extent of results in the stream.
     * <p>
     * This method works on cursors containing {@link Feature} or {@link Geometry} objects.
     * Use {@link #bounds(Function)} to provide an explicit bounds provider.
     * </p>
     *
     * TODO: move this to FeatureStream
     */
    public Bounds bounds() throws IOException {
        return bounds(new Function<T, Bounds>() {
            @Override
            public Bounds apply(T obj) {
                Geometry g = null;
                if (obj instanceof Geometry) {
                    g = (Geometry) obj;
                } else if (obj instanceof Feature) {
                    g = ((Feature) obj).geometry();
                }
                if (g != null) {
                    return new Bounds(g.getEnvelopeInternal());
                }
                return null;
            }
        });
    }

    /**
     * Returns the aggregated spatial extent of results in the stream.
     *
     * @param env Function providing envelopes for objects in the stream.
     */
    public Bounds bounds(Function<T, Bounds> env) throws IOException {
        try (Cursor<T> c = this) {
            Bounds extent = new Bounds();
            extent.setToNull();

            while (c.hasNext()) {
                Bounds e = env.apply(c.next());
                if (!Bounds.isNull(e)) {
                    extent.expandToInclude(e);
                }
            }
            return extent;
        }
    }

    /**
     * Limits the number of results given back by the stream to a fixed size.
     *
     * @param limit The maximum number of objects to return.
     *
     * @return The limited cursor.
     */
    public Cursor<T> limit(Integer limit) {
        return new LimitCursor<>(this, limit);
    }

    static class LimitCursor<T> extends CursorWrapper<T,T> {

        Integer limit;
        Integer count;

        LimitCursor(Cursor<T> delegate, Integer limit) {
            super(delegate);

            this.limit = Objects.requireNonNull(limit, "limit must not be null");
            this.count = 0;
        }

        @Override
        public boolean hasNext() throws IOException  {
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
     * Returns a stream that will skip over the specified number of objects.
     *
     * @param offset The number of objects to skip over.
     *
     * @return The skipped cursor.
     */
    public Cursor<T> skip(Integer offset) {
        return new OffsetCursor<>(this, offset);
    }

    static class OffsetCursor<T> extends CursorWrapper<T,T> {

        Integer offset;

        OffsetCursor(Cursor<T> delegate, Integer offset) {
            super(delegate);

            this.offset = Objects.requireNonNull(offset, "offset must not be null");
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
     * Wraps a stream returning objects that pass a predicate.
     *
     * @param filter The predicate used to filter objects.
     *
     * @return The filtered cursor.
     */
    public Cursor<T> filter(Predicate<T> filter) {
        return new FilterCursor<>(this, filter);
    }

    private static class FilterCursor<T> extends CursorWrapper<T,T> {

        Predicate<T> filter;
        T next;

        FilterCursor(Cursor<T> delegate, Predicate<T> filter) {
            super(delegate);
            this.filter = Objects.requireNonNull(filter, "filter must not be null");
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

    /**
     * Returns a cursor capable of being {@link #rewind()} within a certain number
     * of objects.
     * <p>
     *   The <tt>n</tt> parameter specifies the buffer size. Up to n objects can be
     *   read from the cursor before calling {@link #rewind()}. After n objects have
     *   been read the cursor stops buffering and delegates to the original cursor.
     * </p>
     * @param n The buffer size.
     */
    public Cursor<T> buffer(int n) {
        return new BufferedCursor(this, n);
    }

    /**
     * Rewinds the cursor to it's initial state.
     * <p>
     * Whether a cursor is rewindable or not depends on the implementation. The {@link #buffer(int)} method can be
     * used to create a rewindable cursor.
     * </p>
     * <p>
     */
    public void rewind() {
        throw new UnsupportedOperationException("cursor not rewindable");
    }

    static class BufferedCursor<T> extends CursorWrapper<T,T> {

        int bufferSize;
        List<T> buffer;

        /* buffer index */
        int i = 0;

        /* total read */
        int total = 0;

        BufferedCursor(Cursor delegate, int bufferSize) {
            super(delegate);
            this.bufferSize = Objects.requireNonNull(bufferSize, "bufferSize must not be null");
            this.buffer = new ArrayList<>(bufferSize);
        }

        @Override
        public boolean hasNext() throws IOException {
            if (i < buffer.size()) {
                return true;
            }

            return delegate.hasNext();
        }

        @Override
        public T next() throws IOException {
            if (i < buffer.size()) {
                return buffer.get(i++);
            }

            T next = delegate.next();
            if (buffer.size() < bufferSize) {
                buffer.add(next);
                i++;
            }

            total++;
            return next;
        }

        @Override
        public void rewind() {
            if (total > bufferSize) {
                // past the buffer, can't rewind
                delegate.rewind();
            }

            i = total = 0;
        }
    }

    /**
     * Applies a mapping function to the stream.
     *
     * @param mapper the mapping function.
     *
     * @return The new cursor.
     */
    public <R> Cursor<R> map(final Function<T,R> mapper) {
        Objects.requireNonNull(mapper, "mapper must not be null");
        return new CursorWrapper<T,R>(this) {
            @Override
            public R next() throws IOException {
                return mapper.apply(delegate.next());
            }
        };
    }

    protected static class CursorWrapper<T,R> extends Cursor<R> {

        protected Cursor<T> delegate;

        protected CursorWrapper(Cursor<T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public boolean hasNext() throws IOException {
            return delegate.hasNext();
        }

        @Override
        public R next() throws IOException {
            return (R) delegate.next();
        }

        @Override
        public void rewind() {
            delegate.rewind();
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }
    }
}
