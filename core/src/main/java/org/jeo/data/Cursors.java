package org.jeo.data;

import java.io.IOException;

public class Cursors {

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
        };
    }

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
    }
}

