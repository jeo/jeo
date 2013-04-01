package org.jeo.filter;

public class FilterVisitor {

    public Object visit(Expression expr, Object obj) {
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
        visit(compare.getLeft(), obj);
        visit(compare.getRight(), obj);
        return obj;
    }

    public Object visit(Spatial spatial, Object obj) {
        visit(spatial.getLeft(), obj);
        visit(spatial.getRight(), obj);
        return obj;
    }
}
