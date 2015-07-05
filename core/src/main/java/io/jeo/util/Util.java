/* Copyright 2013 The jeo project. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jeo.util;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Utility class.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class Util {
    public static Charset UTF_8 = Charset.forName("UTF-8"); 

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
        return dot != -1 ? filename.substring(dot+1).toLowerCase(Locale.ROOT) : null;
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
                String.format(Locale.ROOT,"index: %d, size: %d", index, map.size())); 
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

    /**
     * Returns true if the string is null, or is empty.
     */
    public static boolean isEmptyOrNull(String s) {
        return s == null || s.isEmpty();
    }

    /**
     * Returns null if a string is null or empty.
     */
    public static String nullIfEmpty(String s) {
        return "".equals(s) ? null : s;
    }

    /**
     * Appends an object and a variable list of additional objects to an existing collection.
     *
     * @param col The collection.
     * @param first The first object to add.
     * @param rest The remaining object to add.
     *
     * @return The collection.
     */
    public static <T extends Collection> T addAll(T col, Object first, Object... rest) {
        col.add(first);
        for (Object o : rest) {
            col.add(o);
        }
        return col;
    }
}
