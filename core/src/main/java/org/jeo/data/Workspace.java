package org.jeo.data;

import java.io.IOException;
import java.util.Iterator;

public interface Workspace {

    Iterator<String> layers() throws IOException;

    Layer get(String layer) throws IOException;
}
