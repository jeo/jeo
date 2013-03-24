package org.jeo.filter;

/**
 * Identity filter that always returns false.
 */
public class None extends Filter {

    @Override
    public boolean apply(Object obj) {
        return false;
    }

    @Override
    public Object accept(FilterVisitor v, Object obj) {
        return v.visit(this, obj);
    }

}
