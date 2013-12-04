package org.jeo.filter;

/**
 * Predicate that applies a boolean filter for a given input. 
 *   
 * @author Justin Deoliveira, OpenGeo
 */
public abstract class Filter<T> {

    /**
     * Returns a new filter builder.
     */
    public static FilterBuilder build() {
        return new FilterBuilder();
    }

    /**
     * Applies the filter to the specified input.
     * 
     * @param obj The input.
     * 
     * @return The result, <code>true</code> if the filter matches the specific input, otherwise
     *   <code>false</code>.
     */
    public abstract boolean apply(T obj);

    /**
     * Creates a new filter that is a logical AND of this filter and the specified filter.
     */
    public Filter<T> and(Filter<T> other) {
        if (other instanceof All) {
            return this;
        }
        if (other instanceof None) {
            return other;
        }

        return new Logic<T>(Logic.Type.AND, this, other);
    }

    /**
     * Creates a new filter that is a logical OR of this filter and the specified filter.
     */
    public Filter<T> or(Filter<T> other) {
        if (other instanceof All) {
            return other;
        }
        if (other instanceof None) {
            return this;
        }
        return new Logic<T>(Logic.Type.OR, this, other);
    }

    /**
     * Creates a new filter that is the negation of this filter.
     */
    public Filter<T> not() {
        return new Logic<T>(Logic.Type.NOT, this); 
    }

    /**
     * Applies a visitor to the filter.
     */
    public Object accept(FilterVisitor v, Object obj) {
        return v.visit(this, obj);
    }

}
