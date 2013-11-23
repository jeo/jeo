package org.jeo.filter;

/**
 * Expression that just returns the value passed into it.
 * 
 * @author Justin Deoliveira, Boundless
 */
public class Self implements Expression {

    @Override
    public Object evaluate(Object obj) {
        return obj;
    }

    @Override
    public Object accept(FilterVisitor visitor, Object obj) {
        return visitor.visit(this, obj);
    }

}
