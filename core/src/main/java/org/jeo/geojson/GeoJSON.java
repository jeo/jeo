package org.jeo.geojson;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * GeoJSON module utility class.
 * <p>
 * Provides parsing/encoding support for GeoJSON as defined by {@linkplain http://geojson.org}.
 * </p>
 * @author Justin Deoliveira, OpenGeo
 */
public class GeoJSON {

    /**
     * Parses a GeoJSON input stream.
     * 
     * @param in An input stream containing GeoJSON.
     *
     * @return Parsed object.
     * 
     * @throws IOException In the event of a parsing error.
     */
    public static Object read(InputStream in) throws IOException {
        return new GeoJSONReader().read(in);
    }

    /**
     * Encodes an object as GeoJSON to an output stream.
     * <p>
     * This method delegates to {@link GeoJSONWriter#write(Object)}.
     * </p> 
     * @param obj The object to encode.
     * @param out The output stream to write to.
     * 
     * @throws IOException In the event of an encoding error.
     */
    public static void write(Object obj, OutputStream out) throws IOException {
        out.write(toString(obj).getBytes());
    }

    /**
     * Encodes an object as GeoJSON to a string.
     * <p>
     * This method delegates to {@link GeoJSONWriter#write(Object)}.
     * </p> 
     * @param obj The object to encode.
     *
     * @return GeoJSON string. 
     * 
     * @throws IOException In the event of an encoding error.
     */
    public static String toString(Object obj) throws IOException {
        return new GeoJSONWriter().write(obj).toString();
    }
}
