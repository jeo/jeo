package org.jeo.util;

import java.util.Map;

/**
 * Describes a key value pair with a named key and type of value, with an optional default value.
 *  
 * @author Justin Deoliveira, OpenGeo
 */
public class Key<T> {

    String name;
    Class<T> type;
    T def;

    public Key(String name, Class<T> type) {
        this(name, type, null);
    }

    public Key(String name, Class<T> type, T def) {
        this.name = name;
        this.type = type;
        this.def = def;
    }

    /**
     * The name of the key.
     */
    public String getName() {
        return name;
    }

    /**
     * The class of value of the key.
     */
    public Class<T> getType() {
        return type;
    }

    /**
     * The default value for the key value pair, may be <code>null</code>.
     */
    public T getDefault() {
        return def;
    }

    /**
     * Loads the key from a map, converting the raw map value to the value of the key.
     * <p>
     * If the map contains no matching key {@link #getDefault()} is returned.
     * </p>
     */
    public T get(Map<?,Object> map) {
        Object obj = null;
        if (map.containsKey(this)) {
            obj = map.get(this);
        }
        else if (map.containsKey(name)) {
            obj = map.get(name);
        }
        else {
            return def;
        }

        if (obj == null) {
            return null;
        }

        return Convert.to(obj, type);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((def == null) ? 0 : def.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }

    
}
