package org.jeo.util;

import java.io.File;
import java.util.UUID;


/**
 * Utility class.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class Util {

    /**
     * Generates a random UUID.
     * 
     * @see UUID
     */
    public static String uuid() {
        return UUID.randomUUID().toString();
    }

    /**
     * Returns the basename of the file, stripping off the extension if one exists.
     */
    public static String base(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot != -1 ? filename.substring(0, dot) : filename;
    }

    /**
     * Returns the extension of the file, or null if the filename has no extension.
     */
    public static String extension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot != -1 ? filename.substring(dot+1).toLowerCase() : null;
    }

    /**
     * Determines if the file is "empty", meaning it does not exists or has zero length.
     */
    public static boolean isEmpty(File file) {
        return !file.exists() || file.length() == 0;
    }
}
