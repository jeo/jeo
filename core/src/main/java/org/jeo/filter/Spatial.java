package org.jeo.filter;

import org.jeo.geom.Geom;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Filter that applies a spatial comparison operator to two geometry expression operands.  
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class Spatial<T> extends Filter<T> {

    /**
     * Spatial operator type.  
     */
    public static enum Type {
        INTERSECT, TOUCH, DISJOINT, OVERLAP, CROSS, COVER, WITHIN;
    }

    Type type;
    Expression<?> expr1, expr2;

    public Spatial(Type type, Expression<?> expr1, Expression<?> expr2) {
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
        if (o1 == null || o2 == null) {
            throw new IllegalArgumentException("Unable to perform comparison on null operand(s)");
        }

        Geometry g1 = toGeometry(o1);
        Geometry g2 = toGeometry(o2);

        switch(type) {
        case INTERSECT:
            return g1.intersects(g2);
        case TOUCH:
            return g1.touches(g2);
        case OVERLAP:
            return g1.overlaps(g2);
        case DISJOINT:
            return g1.disjoint(g2);
        case CROSS:
            return g1.crosses(g2);
        case COVER:
            return g1.covers(g2);
        case WITHIN:
            return g1.within(g2);
        default:
            throw new IllegalStateException();
        }
    }

    protected Geometry toGeometry(Object o) {
        if (o instanceof Geometry) {
            return (Geometry) o;
        }
        if (o instanceof Envelope) {
            return Geom.toPolygon((Envelope)o);
        }

        throw new IllegalArgumentException("Unable to convert " + o + " to geometry");
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
        Spatial<?> other = (Spatial<?>) obj;
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
