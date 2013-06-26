package org.jeo.filter;

import org.jeo.util.Convert;

/**
 * Filter that applies a binary comparison operator to two expression operands.  
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class Comparison extends Filter {

    /**
     * Comparison operator type.  
     */
    public static enum Type {
        EQUAL("="), NOT_EQUAL("!="), LESS("<"), LESS_OR_EQUAL("<="), GREATER(">"), 
        GREATER_OR_EQUAL(">=");

        String op;

        Type(String op) {
            this.op = op;
        }

        @Override
        public String toString() {
            return op;
        }
    }

    Type type;
    Expression left, right;

    public Comparison(Type type, Expression left, Expression right) {
        this.type = type;
        this.left = left;
        this.right = right;

        if (left == null || right == null) {
            throw new NullPointerException("operands must not be null");
        }
    }

    public Type getType() {
        return type;
    }

    public Expression getLeft() {
        return left;
    }

    public Expression getRight() {
        return right;
    }

    @Override
    public boolean apply(Object obj) {
        Object o1 = left.evaluate(obj);
        Object o2 = right.evaluate(obj);

        return compare(o1, o2);
    }

    protected boolean compare(Object o1, Object o2) {
        if (o1 != null && !o1.getClass().isInstance(o2)) {
            //attempt to convert 
            Object converted = Convert.to(o2, o1.getClass());
            if (converted != null) {
                o2 = converted;
            }
        }

        if (type == Type.EQUAL) {
            return o1 != null ? o1.equals(o2) : o2 == null;
        }
        if (type == Type.NOT_EQUAL) {
            return o1 != null ? !o1.equals(o2) : o2 != null;
        }

        if (o1 == null || o2 == null) {
            throw new IllegalArgumentException("Unable to perform comparison on null operand(s)");
        }

        Comparable<Object> c1 = toComparable(o1);
        Comparable<Object> c2 = toComparable(o2);
        int compare = c1.compareTo(c2);

        switch(type) {
        case LESS:
            return compare < 0;
        case LESS_OR_EQUAL:
            return compare <= 0;
        case GREATER:
            return compare > 0;
        case GREATER_OR_EQUAL:
            return compare >= 0;
        default:
            throw new IllegalStateException();
        }
    }

    @SuppressWarnings("unchecked")
    protected Comparable<Object> toComparable(Object o) {
        if (o instanceof Comparable) {
            return (Comparable<Object>) o;
        }
        throw new IllegalArgumentException("Unable to convert " + o + " to comparable");
    }

    @Override
    public Object accept(FilterVisitor v, Object obj) {
        return v.visit(this, obj);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((left == null) ? 0 : left.hashCode());
        result = prime * result + ((right == null) ? 0 : right.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Comparison other = (Comparison) obj;
        if (left == null) {
            if (other.left != null)
                return false;
        } else if (!left.equals(other.left))
            return false;
        if (right == null) {
            if (other.right != null)
                return false;
        } else if (!right.equals(other.right))
            return false;
        if (type != other.type)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(left).append(" ").append(type).append(" ").append(right)
            .toString();
    }
}
