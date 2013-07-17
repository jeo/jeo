package org.jeo.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An expression composed of other expressions.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class Mixed implements Expression {

    List<Expression> exprs = new ArrayList<Expression>();

    public Mixed(Expression... exprs) {
        this(Arrays.asList(exprs));
    }

    public Mixed(List<Expression> exprs) {
        this.exprs = new ArrayList<Expression>(exprs);
    }

    public List<Expression> getExpressions() {
        return exprs;
    }

    @Override
    public Object evaluate(Object obj) {
        List<Object> result = new ArrayList<Object>();
        for (Expression expr: exprs) {
            result.add(expr.evaluate(obj));
        }
        return result;
    }

    @Override
    public Object accept(FilterVisitor visitor, Object obj) {
        return visitor.visit(this, obj);
    }

    /**
     * Appends an expression to the mix.
     */
    public Mixed append(Expression expr) {
        Mixed mixed = new Mixed(exprs);
        mixed.exprs.add(expr);
        return mixed;
    }
}
