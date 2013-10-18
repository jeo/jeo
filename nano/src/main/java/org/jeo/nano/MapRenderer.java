package org.jeo.nano;

import java.io.IOException;
import java.io.OutputStream;

import org.jeo.map.Map;

public interface MapRenderer {

    void render(Map map, OutputStream output) throws IOException;
}
