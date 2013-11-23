package org.jeo.filter;

/**
 * Identity filter that always returns false.
 */
public class None<T> extends Filter<T> {

    @Override
    public boolean apply(T obj) {
        return false;
    }

    @Override
    public Object accept(FilterVisitor v, Object obj) {
        return v.visit(this, obj);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof None;
    }

    @Override
    public int hashCode() {
        return None.class.getName().hashCode();
    }

    @Override
    public String toString() {
        return "None";
    }
}
