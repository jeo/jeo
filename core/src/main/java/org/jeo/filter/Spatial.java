/* Copyright 2013 The jeo project. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jeo.filter;

import org.jeo.geom.Envelopes;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Filter that applies a spatial comparison operator to two geometry expression operands.  
 * 
 * @author Justin Deoliveira, OpenGeo
 * 
 * TODO: use prepared geometries
 */
public class Spatial<T> extends Filter<T> {

    /**
     * Spatial operator type.  
     */
    public static enum Type {
        INTERSECT, TOUCH, DISJOINT, OVERLAP, CROSS, COVER, WITHIN, CONTAIN, BBOX;
    }

    Type type;
    Expression left, right;

    public Spatial(Type type, Expression left, Expression right) {
        this.type = type;
        this.left = left;
        this.right = right;
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
    public boolean apply(T obj) {
        Object o1 = left.evaluate(obj);
        Object o2 = right.evaluate(obj);

        return compare(o1, o2);
    }

    protected boolean compare(Object o1, Object o2) {
        if (o1 == null || o2 == null) {
            throw new IllegalArgumentException("Unable to perform comparison on null operand(s)");
        }

        if (type == Type.BBOX) {
            // only need to compare envelopes
            Envelope e1 = toEnvelope(o1);
            Envelope e2 = toEnvelope(o2);

            return e1.intersects(e2);
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
        case CONTAIN:
            return g1.contains(g2);
        default:
            throw new IllegalStateException();
        }
    }

    protected Envelope toEnvelope(Object o) {
        if (o instanceof Envelope) {
            return (Envelope) o;
        }

        Geometry g = toGeometry(o);
        if (g == null) {
            throw new IllegalArgumentException("Unable to convert " + o + " to envelope");
        }

        return g.getEnvelopeInternal();
    }

    protected Geometry toGeometry(Object o) {
        if (o instanceof Geometry) {
            return (Geometry) o;
        }
        if (o instanceof Envelope) {
            return Envelopes.toPolygon((Envelope)o);
        }

        throw new IllegalArgumentException("Unable to convert " + o + " to geometry");
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
        Spatial<?> other = (Spatial<?>) obj;
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
