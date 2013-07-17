package org.jeo.carto;

import java.io.IOException;
import java.io.Reader;

import org.jeo.map.Style;
import org.jeo.util.Convert;

public class Carto {

    public static Style parse(Object css) throws IOException {
        Reader reader = Convert.toReader(css).get("Unable to handle input: " + css);
        return new CartoParser().parse(reader);
    }
}
