package org.jeo.util;

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
}
