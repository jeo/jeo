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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Describes a key value pair with a named key and type of value, with an optional default value.
 *  
 * @author Justin Deoliveira, OpenGeo
 */
public class Key<T> {

    /**
     * key names
     */
    List<String> names = new ArrayList<>();

    /**
     * type of the key
     */
    Class<T> type;

    /**
     * default value
     */
    T def;

    /**
     * multi-valued or not
     */
    boolean multi;

    public Key(String name, Class<T> type) {
        this(name, type, null);
    }

    public Key(String name, Class<T> type, T def) {
        this.names.add(name);
        this.type = type;
        this.def = def;
    }

    /**
     * The name of the key.
     */
    public String name() {
        return names.get(0);
    }

    /**
     * The class of value of the key.
     */
    public Class<T> type() {
        return type;
    }

    /**
     * The default value for the key value pair, may be <code>null</code>.
     */
    public T def() {
        return def;
    }

    /**
     * Whether the key is multi valued.
     */
    public boolean multi() {
        return multi;
    }

    /**
     * Sets the key multi valued flag.
     */
    public Key multi(boolean multi) {
        this.multi = multi;
        return this;
    }

    /**
     * Adds a key alias.
     */
    public Key alias(String... names) {
        for (String n : names) {
            this.names.add(n);
        }
        return this;
    }

    /**
     * Loads the key from a map, converting the raw map value to the value of the key.
     * <p>
     * If the map contains no matching key {@link #def()} is returned.
     * </p>
     */
    public T get(Map<?,Object> map) {
        if (!in(map)) {
            return def;
        }

        Object raw = raw(map);
        if (raw == null || "".equals(raw)) {
            return null;
        }

        return parseOrConvert(raw);
    }

    /**
     * Loads the multi-valued key from a map, converting the raw map value to the value of the key.
     * <p>
     * If the map contains no matching key, a list containing {@link #def()} is returned. If no default
     * was specified an empty list is returned.
     * </p>
     */
    public List<T> all(Map<?,Object> map) {
        if (!multi) {
            throw new IllegalStateException("key is not multi valued");
        }

        if (!in(map)) {
            return def != null ? Arrays.asList(def) : null;
        }

        Object raw = raw(map);
        if (raw == null) {
            return Arrays.asList();
        }

        if (multi) {
            // if string, split on delimiter TODO: make delimiter configurable
            if (raw instanceof String) {
                raw = Arrays.asList(raw.toString().split("\\s*,\\s*"));
            }

            if (raw instanceof Collection) {
                // process collection and parse elements
                List<T> list = new ArrayList<>();
                for (Object obj : ((Collection)raw)) {
                    list.add(parseOrConvert(obj));
                }
                return list;
            }
        }

        return Arrays.asList(parseOrConvert(raw));
    }

    /**
     * Returns the raw value in the map for the key, with no conversions.
     */
    public Object raw(Map<?,Object> map) {
        if (map.containsKey(this)) {
            return map.get(this);
        }

        for (String name : names) {
            if (map.containsKey(name)) {
                return map.get(name);
            }
        }
        return null;
    }

    /**
     * Determines if the key exists in a map.
     */
    public boolean in(Map<?, Object> map) {
        if (map == null) {
            return false;
        }
        if (map.containsKey(this)) {
            return true;
        }
        for (String name : names) {
            if (map.containsKey(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Subclass hook to parse raw object.
     *
     */
    protected T parse(Object raw) {
        return null;
    }

    T parseOrConvert(Object raw) {
        T obj = parse(raw);
        return obj != null ? obj : Convert.to(raw, type).get("Unable to convert " + raw + " to " + type.getName());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((def == null) ? 0 : def.hashCode());
        result = prime * result + ((names == null) ? 0 : names.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Key<?> other = (Key<?>) obj;
        if (def == null) {
            if (other.def != null)
                return false;
        } else if (!def.equals(other.def))
            return false;
        if (names == null) {
            if (other.names != null)
                return false;
        } else if (!names.equals(other.names))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return name();
    }
}
