/* Copyright 2015 The jeo project. All rights reserved.
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
package io.jeo.geotools;

import io.jeo.filter.All;
import io.jeo.filter.Comparison;
import io.jeo.filter.FilterWalker;
import io.jeo.filter.Function;
import io.jeo.filter.Id;
import io.jeo.filter.In;
import io.jeo.filter.Like;
import io.jeo.filter.Literal;
import io.jeo.filter.Logic;
import io.jeo.filter.Mixed;
import io.jeo.filter.None;
import io.jeo.filter.Null;
import io.jeo.filter.Property;
import io.jeo.filter.Spatial;
import io.jeo.filter.Spatial.Type;
import io.jeo.util.Optional;
import io.jeo.util.Supplier;
import org.geotools.filter.expression.InternalVolatileFunction;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.identity.Identifier;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class FilterConverter extends FilterWalker<Object> {

    FilterFactory2 filterFactory;
    SimpleFeatureType featureType;

    public FilterConverter(FilterFactory2 filterFactory, SimpleFeatureType featureType) {
        this.filterFactory = filterFactory;
        this.featureType = featureType;
    }

    public Optional<Expression> convert(io.jeo.filter.Expression e) {
        return Optional.of((Expression)e.accept(this, null));
    }

    public Optional<Filter> convert(io.jeo.filter.Filter f) {
        return Optional.of((Filter)f.accept(this, null));
    }

    @Override
    public Object visit(Literal literal, Object obj) {
        return filterFactory.literal(literal.evaluate(null));
    }

    @Override
    public Object visit(Property property, Object obj) {
        return filterFactory.property(property.property());
    }

    @Override
    public Object visit(Mixed mixed, Object obj) {
        List<Expression> l = new ArrayList<Expression>();
        for (io.jeo.filter.Expression e : mixed.expressions()) {
            l.add(expr(e, obj));
        }

        return filterFactory.function("strConcat", l.toArray(new Expression[l.size()]));
    }

    @Override
    public Object visit(final Function function, Object obj) {
        return new InternalVolatileFunction(function.name()) {
            @Override
            public Object evaluate(Object object) {
                if (object instanceof SimpleFeature) {
                    return function.evaluate(GT.feature((SimpleFeature) object));
                }
                return function.evaluate(object);
                //throw new IllegalArgumentException(
                //    "unable to handle function input: " + object);
            }
        };
    }

    @Override
    public Object visit(All<?> all, Object obj) {
        return Filter.INCLUDE;
    }

    @Override
    public Object visit(None<?> none, Object obj) {
        return Filter.EXCLUDE;
    }

    @Override
    public Object visit(Id<?> id, Object obj) {
        Set<Identifier> ids = new LinkedHashSet<>();
        for (io.jeo.filter.Expression expr : id.ids()) {
            ids.add(filterFactory.featureId(eval(expr).to(String.class).orElse(expr.toString())));
        }
        return filterFactory.id(ids);
    }

    @Override
    public Object visit(Logic<?> logic, Object obj) {
        List<Filter> filters = new ArrayList<>();
        for (io.jeo.filter.Filter f : logic.parts()) {
            filters.add(filter(f, obj));
        }

        switch(logic.type()) {
            case AND:
                return filterFactory.and(filters);
            case OR:
                return filterFactory.or(filters);
            case NOT:
                if (filters.isEmpty()) {
                    throw new IllegalStateException("filter list is empty");
                }
                return filterFactory.not(filters.iterator().next());
            default:
                throw new IllegalStateException("Unsupported logic type: " + logic.type());
        }
    }

    @Override
    public Object visit(Comparison<?> compare, Object obj) {
        Expression e1 = expr(compare.left(), obj);
        Expression e2 = expr(compare.right(), obj);

        switch(compare.type()) {
            case EQUAL:
                return filterFactory.equal(e1, e2, true);
            case NOT_EQUAL:
                return filterFactory.notEqual(e1, e2, true);
            case LESS:
                return filterFactory.less(e1, e2, true);
            case LESS_OR_EQUAL:
                return filterFactory.lessOrEqual(e1, e2, true);
            case GREATER:
                return filterFactory.greater(e1, e2, true);
            case GREATER_OR_EQUAL:
                return filterFactory.greaterOrEqual(e1, e2, true);
            default:
                throw new IllegalStateException("Unsupported comparison type: " + compare.type());
        }

    }

    @Override
    public Object visit(Spatial<?> spatial, Object obj) {
        Expression e1 = expr(spatial.left(), obj);
        Expression e2 = expr(spatial.right(), obj);
        Double dist = null;

        if (spatial.type() == Type.BEYOND || spatial.type() == Type.DWITHIN) {
            dist =  eval(spatial.distance()).to(Double.class).orElseThrow(new Supplier<RuntimeException>() {
                @Override
                public RuntimeException get() {
                    throw new IllegalArgumentException("Unable to convert distance to double");
                }
            });
        }

        switch(spatial.type()) {
            case EQUALS:
                return filterFactory.equals(e1, e2);
            case INTERSECTS:
                return filterFactory.intersects(e1, e2);
            case TOUCHES:
                return filterFactory.touches(e1, e2);
            case DISJOINT:
                return filterFactory.disjoint(e1, e2);
            case OVERLAPS:
                return filterFactory.overlaps(e1, e2);
            case CROSSES:
                return filterFactory.crosses(e1, e2);
            case WITHIN:
                return filterFactory.within(e1, e2);
            case CONTAINS:
                return filterFactory.contains(e1, e2);
            case DWITHIN:
                return filterFactory.dwithin(e1, e2, dist, null);
            case BEYOND:
                return filterFactory.beyond(e1, e2, dist, null);
            case COVERS:
            case BBOX:
            default:
                throw new IllegalArgumentException("Unsupported spatial type: " + spatial.type());
        }
    }

    @Override
    public Object visit(Like<?> like, Object obj) {
        return filterFactory.like(expr(like.property(), obj), like.pattern().toString(), ".*", "?", "\\");
    }

    @Override
    public Object visit(io.jeo.filter.Math math, Object obj) {
        Expression e1 = expr(math.left(), obj);
        Expression e2 = expr(math.right(), obj);

        switch(math.operator()) {
            case '+':
                return filterFactory.add(e1, e2);
            case '-':
                return filterFactory.subtract(e1, e2);
            case '*':
                return filterFactory.multiply(e1, e2);
            case '%':
            case '/':
                return filterFactory.divide(e1, e2);
            default:
                throw new IllegalArgumentException("Unsupported math operator: " + math.operator());
        }
    }

    @Override
    public Object visit(Null<?> isNull, Object obj) {
        Expression e = expr(isNull.property(), obj);
        Filter f;

        // hack, is null with a non-existing property returns true, we want it to be false
        if (featureType != null && e.evaluate(featureType) == null) {
            f = Filter.EXCLUDE;
        }
        else {
            f = filterFactory.isNull(e);
        }

        if (isNull.negated()) {
            f = filterFactory.not(f);
        }
        return f;
    }

    @Override
    public Object visit(In<?> in, Object obj) {
        // convert to a big or
        Expression p = expr(in.property(), obj);

        List<Filter> list = new ArrayList<>();
        for (io.jeo.filter.Expression e : in.values()) {
            list.add(filterFactory.equal(p, expr(e,obj), true));
        }

        return filterFactory.or(list);
    }

    Filter filter(io.jeo.filter.Filter f, Object obj) {
        Object result = f.accept(this, obj);
        if (result instanceof Filter) {
            return (Filter) result;
        }

        throw new IllegalArgumentException("Unable to convert filter: " + f);
    }

    Expression expr(io.jeo.filter.Expression e, Object obj) {
        Object result = e.accept(this, obj);
        if (result instanceof Expression) {
            return (Expression) result;
        }

        throw new IllegalArgumentException("Unable to convert expression: " + e);
    }

    Optional<Object> eval(io.jeo.filter.Expression expr) {
        if (expr == null) {
            return Optional.empty();
        }
        return Optional.of(expr.evaluate(null));
    }
}
