package org.jeo.filter;

/**
 * Predicate that applies a boolean filter for a given input. 
 *   
 * @author Justin Deoliveira, OpenGeo
 */
public abstract class Filter {

    /**
     * Returns true if the filter is <tt>null</tt> or equal to {@link #TRUE}.
     */
    public static boolean isTrueOrNull(Filter f) {
        return f == TRUE || f == null;
    }

    /**
     * Returns true if the filter is <tt>null</tt> or equal to {@link #FALSE}.
     */
    public static boolean isFalseOrNull(Filter f) {
        return f == FALSE || f == null;
    }

    /**
     * Identity filter that always returns true.
     */
    public static final Filter TRUE = new All();

    /**
     * Identity filter that always returns false.
     */
    public static final Filter FALSE = new None();

    /**
     * Applies the filter to the specified input.
     * 
     * @param obj The input.
     * 
     * @return The result, <code>true</code> if the filter matches the specific input, otherwise
     *   <code>false</code>.
     */
    public abstract boolean apply(Object obj);

    /**
     * Creates a new filter that is a logical AND of this filter and the specified filter.
     */
    public Filter and(Filter other) {
        if (other == Filter.TRUE) {
            return this;
        }
        if (other == Filter.FALSE) {
            return other;
        }

        return new Logic(Logic.Type.AND, this, other);
    }

    /**
     * Creates a new filter that is a logical OR of this filter and the specified filter.
     */
    public Filter or(Filter other) {
        if (other == Filter.TRUE) {
            return other;
        }
        if (other == Filter.FALSE) {
            return this;
        }
        return new Logic(Logic.Type.OR, this, other);
    }

    /**
     * Creates a new filter that is the negation of this filter.
     */
    public Filter not() {
        return new Logic(Logic.Type.NOT, this); 
    }

    /**
     * Applies a visitor to the filter.
     */
    public abstract Object accept(FilterVisitor v, Object obj);
}
