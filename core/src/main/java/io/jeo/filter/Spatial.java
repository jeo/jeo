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
package io.jeo.filter;

import io.jeo.geom.Envelopes;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import java.util.Locale;
import java.util.Objects;

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
        EQUALS, INTERSECTS, TOUCHES, DISJOINT, OVERLAPS, CROSSES, COVERS, BBOX,
        WITHIN {
            @Override
            public Type invert() {
                return CONTAINS;
            }
        },
        CONTAINS {
            @Override
            public Type invert() {
                return WITHIN;
            }
        },
        DWITHIN {
            @Override
            public Type invert() {
                return BEYOND;
            }
        },
        BEYOND {
            @Override
            public Type invert() {
                return DWITHIN;
            }
        };

        public Type invert() {
            return this;
        }
    }

    final Type type;
    final Expression left, right, distance;

    public Spatial(Type type, Expression left, Expression right, Expression distance) {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(left, "operands must not be null");
        Objects.requireNonNull(right, "operands must not be null");

        switch (type) {
            case DWITHIN: case BEYOND:
                if (distance == null) {
                    throw new IllegalArgumentException(String.format(Locale.ROOT,"operator: %s, requires distance value", type));
                }
        }
        this.type = type;
        this.left = left;
        this.right = right;
        this.distance = distance;
    }

    public Type type() {
        return type;
    }

    public Expression distance() {
        return distance;
    }

    public Expression left() {
        return left;
    }

    public Expression right() {
        return right;
    }

    /**
     * Normalizes the filter.
     * <p>
     * A "normal" filter is one where a {@link Property} is compared to another expression.
     * This method returns <code>null</code> if the filter is not normal.
     * </p>
     */
    public Spatial<T> normalize() {
        if (left instanceof Property) {
            return this;
        }
        else if (right instanceof Property) {
            // flip
            return new Spatial<>(type.invert(), right, left, distance);
        }
        else {
            return null;
        }
    }

    @Override
    public boolean test(T obj) {
        Object o1 = left.evaluate(obj);
        Object o2 = right.evaluate(obj);
        Number d = (Number) (distance == null ? null : distance.evaluate(obj));
        return compare(o1, o2, d);
    }

    protected boolean compare(Object o1, Object o2, Number d) {
        if (o1 == null || o2 == null) {
            return false;
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
        case EQUALS:
            return g1.equalsTopo(g2);
        case INTERSECTS:
            return g1.intersects(g2);
        case TOUCHES:
            return g1.touches(g2);
        case OVERLAPS:
            return g1.overlaps(g2);
        case DISJOINT:
            return g1.disjoint(g2);
        case CROSSES:
            return g1.crosses(g2);
        case COVERS:
            return g1.covers(g2);
        case WITHIN:
            return g1.within(g2);
        case CONTAINS:
            return g1.contains(g2);
        case DWITHIN:
            return g1.isWithinDistance(g2, d.doubleValue());
        case BEYOND:
            return !g1.isWithinDistance(g2, d.doubleValue());
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
            return Envelopes.toPolygon((Envelope) o);
        }

        throw new IllegalArgumentException("Unable to convert " + o + " to geometry");
    }

    @Override
    public <R> R accept(FilterVisitor<R> v, Object obj) {
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
        if (distance == null) {
            if (other.distance != null) {
                return false;
            }
        } else if (!distance.equals(other.distance)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder().append(left).
                append(" ").append(type).append(" ").append(right);
        if (distance != null) {
            buf.append(" ").append(distance);
        }
        return buf.toString();
    }
}
