package org.jeo.util;

import java.io.File;

/**
 * Utility class.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class Util {

    /**
     * Converts an object to a {@link File}.
     * 
     * @return The file object or <tt>null</tt> if no conversion was found.
     */
    public static File toFile(Object obj) {
        if (obj == null) {
            return null;
        }

        if (obj instanceof File) {
            return (File) obj;
        }

        if (obj instanceof String) {
            return new File((String)obj);
        }

        return null;
    }

    public static Boolean toBoolean(Object obj) {
        if (obj == null) {
            return null;
        }

        if (obj instanceof Boolean) {
            return (Boolean) obj;
        }

        if (obj instanceof String) {
            return Boolean.parseBoolean((String)obj);
        }

        return null;
    }
}
