package org.jeo.filter;

/**
 * Identity filter that always returns true.
 */
public class All extends Filter {

    @Override
    public boolean apply(Object obj) {
        return true;
    }

    @Override
    public Object accept(FilterVisitor v, Object obj) {
        return v.visit(this, obj);
    }

}
