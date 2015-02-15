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
     * Returns a empty cursor object.
     */
    public static <T> Cursor<T> empty() {
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
        };
    }

    /**
     * Returns a cursor containing a single object.
     */
    public static <T> Cursor<T> single(T obj) {
        return new SingleCursor<T>(obj);
    }

    private static class SingleCursor<T> extends Cursor<T> {

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

    private static class IteratorCursor<T> extends Cursor<T> {
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
        }
    }
}

