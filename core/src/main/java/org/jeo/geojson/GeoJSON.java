package org.jeo.geojson;

import java.io.IOException;
import java.io.InputStream;

import com.vividsolutions.jts.geom.Geometry;

public class GeoJSON {

    public static Object read(InputStream in) throws IOException {
        return new GeoJSONReader().read(in);
    }

    public static String toString(Geometry geom) {
        return new GeoJSONWriter().geometry(geom).toString();
    }
}
