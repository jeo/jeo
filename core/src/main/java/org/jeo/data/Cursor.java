package org.jeo.data;

import java.io.Closeable;
import java.io.IOException;

public interface Cursor<T> extends Closeable, Iterable<T> {

    boolean hasNext() throws IOException;

    T next() throws IOException;
}
