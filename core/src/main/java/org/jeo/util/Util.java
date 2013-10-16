package org.jeo.util;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
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

    public static <K,V> V get(Map<K,V> map, int index) {
        checkIndex(index, map);
        Iterator<V> it = map.values().iterator();
        for (int i = 0; it.hasNext() && i < index; i++, it.next());

        return it.next();
    }

    public static <K,V> void set(Map<K,V> map, int index, V value) {
        checkIndex(index, map);
        Iterator<K> it = map.keySet().iterator();
        for (int i = 0; it.hasNext() && i < index; i++, it.next());

        map.put(it.next(), value);
    }

    static void checkIndex(int index, Map<?,?> map) {
        if (index >= map.size()) {
            throw new IndexOutOfBoundsException(
                String.format("index: %d, size: %d", index, map.size())); 
        }
    }

    public static Map<Object,Object> map(Object... kv) {
        if (kv.length % 2 != 0) {
            throw new IllegalArgumentException("odd number of argumets");
        }
        LinkedHashMap<Object, Object> map = new LinkedHashMap<Object, Object>();
        for (int i = 0; i < kv.length; i+=2) {
            map.put(kv[i].toString(), kv[i+1]);
        }
        return map;
    }
}
