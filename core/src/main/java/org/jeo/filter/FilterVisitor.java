package org.jeo.filter;

public class FilterVisitor {

    public Object visit(Literal literal, Object obj) {
        return obj;
    }

    public Object visit(Property property, Object obj) {
        return obj;
    }

    public Object visit(Function function, Object obj) {
        return obj;
    }

    public Object visit(All all, Object obj) {
        return obj;
    }

    public Object visit(None none, Object obj) {
        return obj;
    }

    public Object visit(Id id, Object obj) {
        return obj;
    }

    public Object visit(Logic logic, Object obj) {
        for (Filter f : logic.getParts()) {
            f.accept(this, obj);
        }
        return obj;
    }

    public Object visit(Comparison compare, Object obj) {
        if (compare.getLeft() != null) {
            compare.getLeft().accept(this, obj);
        }
        if (compare.getRight() != null) {
            compare.getRight().accept(this, obj);
        }
        return obj;
    }

    public Object visit(Spatial spatial, Object obj) {
        if (spatial.getLeft() != null) {
            spatial.getLeft().accept(this, obj);
        }
        if (spatial.getRight() != null) {
            spatial.getRight().accept(this, obj);
        }
        
        return obj;
    }
}
