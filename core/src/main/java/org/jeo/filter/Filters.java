package org.jeo.filter;

/**
 * Filter utility class.
 * 
 * @author Justin Deoliveira, Boundless
 */
public class Filters {
    /**
     * Returns true if the filter is <tt>null</tt> or equal to {@link #TRUE}.
     */
    public static boolean isTrueOrNull(Filter<?> f) {
        return f instanceof All || f == null;
    }

    /**
     * Returns true if the filter is <tt>null</tt> or equal to {@link #FALSE}.
     */
    public static boolean isFalseOrNull(Filter<?> f) {
        return f instanceof None || f == null;
    }

    /**
     * Returns a filter that always evaluates to true.
     */
    public static <T> Filter<T> all() {
        return new All<T>();
    }

    /**
     * Returns a filter that always evaluates to true.
     */
    public static <T> Filter<T> none() {
        return new None<T>();
    }
}
