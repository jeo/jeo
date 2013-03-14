package org.jeo.shp;

import static org.jeo.Tests.*;
import java.io.File;
import java.io.IOException;

/**
 * Provides access to Shapefile test datasets.
 */
public class ShpData {

    /**
     * Returns the U.S. Cencus Berueau "states" shapefile file consisting of 49 states, excluding
     * Alaska and Hawaii, including Distict of Columbia.
     */
    public static Shapefile states() throws IOException {
        File dir = unzip(ShpData.class.getResourceAsStream("states.shp.zip"), newTmpDir());
        return new Shapefile(new File(dir, "states.shp"));
    }
}
