package org.jeo.geojson;

import java.io.IOException;
import java.io.InputStream;

import com.vividsolutions.jts.geom.Geometry;

public class GeoJSON {

    public static Object read(InputStream in) {
        try {
            return new GeoJSONReader().read(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String toString(Geometry geom) {
        return new GeoJSONWriter().geometry(geom).toString();
    }
}
