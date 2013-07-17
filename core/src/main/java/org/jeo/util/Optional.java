package org.jeo.util;

/**
 * Utility object for dealing with references that may be null.
 * <p>
 * This class is inspired by guava's Optional class.
 * </p> 
 * @author Justin Deoliveira, OpenGeo
 */
public class Optional<T> {

    public static <X> Optional<X> of(X value) {
        return new Optional<X>(value);
    }

    public static <X> Optional<X> nil(Class<X> clazz) {
        return new Optional<X>(null);
    }

    T value;

    public Optional(T value) {
        this.value = value;
    }

    public boolean has() {
        return value != null;
    }

    public T get() {
        return get("null value");
    }

    public T get(String errmsg) {
        if (value == null) {
            throw new IllegalArgumentException(errmsg);
        }
        return value;
    }

    public T or(T fallback) {
        return value != null ? value : fallback;
    }
}
