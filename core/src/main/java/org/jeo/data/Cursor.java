package org.jeo.data;

import java.io.Closeable;
import java.io.IOException;

public interface Cursor<T> extends Closeable {

    boolean hasNext() throws IOException;

    T next() throws IOException;
}
