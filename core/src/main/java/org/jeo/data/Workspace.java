package org.jeo.data;

import java.io.IOException;
import java.util.Iterator;

import org.jeo.feature.Schema;

public interface Workspace {

    Vector create(Schema schema) throws IOException;

    Iterator<String> layers() throws IOException;

    Layer get(String layer) throws IOException;
}
