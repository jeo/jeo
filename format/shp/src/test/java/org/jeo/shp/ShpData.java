package org.jeo.shp;

import static org.jeo.Tests.newTmpDir;
import static org.jeo.Tests.unzip;

import java.io.File;
import java.io.IOException;

/**
 * Provides access to Shapefile test datasets.
 */
public class ShpData {

    /**
     * A simple point shapefile, taken from the GeoServer SLD cookbook.
     */
    public static ShpDataset point() throws IOException {
        return shp("point");
    }

    /**
     * A simple line shapefile, taken from the GeoServer SLD cookbook.
     */
    public static ShpDataset line() throws IOException {
        return shp("line");
    }

    /**
     * A simple polygon shapefile, taken from the GeoServer SLD cookbook.
     */
    public static ShpDataset poly() throws IOException {
        return shp("polygon");
    }

    /**
     * Returns the U.S. Census Bureau "states" shapefile file consisting of 49 states, excluding
     * Alaska and Hawaii, including Distinct of Columbia.
     */
    public static ShpDataset states() throws IOException {
        return shp("states");
    }

    static ShpDataset shp(String name) throws IOException {
        File dir = unzip(ShpData.class.getResourceAsStream(name + ".shp.zip"), newTmpDir());
        return new ShpDataset(new File(dir, name+".shp"));
    }
}
