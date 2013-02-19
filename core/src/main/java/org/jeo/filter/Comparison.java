package org.jeo.filter;

/**
 * Filter that applies a binary comparison operator to two expression operands.  
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class Comparison<T> extends Filter<T> {

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
    Expression<?> expr1, expr2;

    public Comparison(Type type, Expression<?> expr1, Expression<?> expr2) {
        this.type = type;
        this.expr1 = expr1;
        this.expr2 = expr2;
    }

    @Override
    public boolean apply(T obj) {
        Object o1 = expr1.evaluate(obj);
        Object o2 = expr2.evaluate(obj);

        return compare(o1, o2);
    }

    protected boolean compare(Object o1, Object o2) {
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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((expr1 == null) ? 0 : expr1.hashCode());
        result = prime * result + ((expr2 == null) ? 0 : expr2.hashCode());
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
        Comparison<?> other = (Comparison<?>) obj;
        if (expr1 == null) {
            if (other.expr1 != null)
                return false;
        } else if (!expr1.equals(other.expr1))
            return false;
        if (expr2 == null) {
            if (other.expr2 != null)
                return false;
        } else if (!expr2.equals(other.expr2))
            return false;
        if (type != other.type)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(expr1).append(" ").append(type).append(" ").append(expr2)
            .toString();
    }
}
