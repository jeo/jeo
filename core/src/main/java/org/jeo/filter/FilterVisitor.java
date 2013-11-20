package org.jeo.filter;

/**
 * Visitor for filter classes.
 * 
 * @author Justin Deoliveira, Boundless
 */
public class FilterVisitor {

    public Object visit(Self self, Object obj) {
        return obj;
    }

    public Object visit(Literal literal, Object obj) {
        return obj;
    }

    public Object visit(Property property, Object obj) {
        return obj;
    }

    public Object visit(Function function, Object obj) {
        return obj;
    }

    public Object visit(Mixed mixed, Object obj) {
        for (Expression e : mixed.getExpressions()) {
            e.accept(this, obj);
        }
        return obj;
    }

    public Object visit(Expression expr, Object obj) {
        return obj;
    }

    public Object visit(All<?> all, Object obj) {
        return obj;
    }

    public Object visit(None<?> none, Object obj) {
        return obj;
    }

    public Object visit(Id<?> id, Object obj) {
        return obj;
    }

    public Object visit(Logic<?> logic, Object obj) {
        for (Filter<?> f : logic.getParts()) {
            f.accept(this, obj);
        }
        return obj;
    }

    public Object visit(Comparison<?> compare, Object obj) {
        if (compare.getLeft() != null) {
            compare.getLeft().accept(this, obj);
        }
        if (compare.getRight() != null) {
            compare.getRight().accept(this, obj);
        }
        return obj;
    }

    public Object visit(Spatial<?> spatial, Object obj) {
        if (spatial.getLeft() != null) {
            spatial.getLeft().accept(this, obj);
        }
        if (spatial.getRight() != null) {
            spatial.getRight().accept(this, obj);
        }
        
        return obj;
    }

    public Object visit(TypeOf<?> inst, Object obj) {
        inst.getExpression().accept(this, obj);
        return obj;
    }

    public Object visit(Filter<?> filter, Object obj) {
        return obj;
    }
}
