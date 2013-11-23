package org.jeo.filter;

/**
 * Filter that tests the type of an object.
 * 
 * @author Justin Deoliveira, Boundless
 */
public class TypeOf<T> extends Filter<T> {

    Expression expr;
    Class<?> type;

    public TypeOf(Expression expr, Class<?> type) {
        this.expr = expr;
        this.type = type;
    }

    public Class<?> getType() {
        return type;
    }

    public Expression getExpression() {
        return expr;
    }

    @Override
    public boolean apply(Object obj) {
        obj = expr.evaluate(obj);

        if (obj instanceof Class) {
            return type.isAssignableFrom((Class)obj);
        }
        return type.isInstance(obj);
    }

    @Override
    public Object accept(FilterVisitor v, Object obj) {
        return v.visit(this, obj);
    }

}
