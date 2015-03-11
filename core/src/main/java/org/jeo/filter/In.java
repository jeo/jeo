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

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 'In' predicate filter. Returns true if the value of the property is
 *  present in the values list.
 *
 * @author Ian Schneider <ischneider@boundlessgeo.com>
 */
public class In<T> extends Filter<T> {

    final Property prop;
    final List<Expression> values;
    final boolean not;

    public In(Property prop, List<? extends Expression> values, boolean not) {
        Objects.requireNonNull(prop, "property must not be null");
        Objects.requireNonNull(values, "values must not be null");
        this.prop = prop;
        this.values = Collections.unmodifiableList(values);
        this.not = not;
    }

    public boolean isNegated() {
        return not;
    }

    public Property property() {
        return prop;
    }

    public List<Expression> values() {
        return values;
    }

    @Override
    public boolean test(T obj) {
        Object evaluate = prop.evaluate(obj);
        boolean result = false;
        for (int i = 0; i < values.size() && !result; i++) {
            Object val = values.get(i).evaluate(obj);
            if (val != null) {
                result = val.equals(evaluate);
            }
        }
        return not != result;
    }

    @Override
    public Object accept(FilterVisitor v, Object obj) {
        return v.visit(this, obj);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.prop != null ? this.prop.hashCode() : 0);
        hash = 89 * hash + (this.values != null ? this.values.hashCode() : 0);
        hash = 89 * hash + (this.not ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final In<?> other = (In<?>) obj;
        if (this.prop != other.prop && (this.prop == null || !this.prop.equals(other.prop))) {
            return false;
        }
        if (this.values != other.values && (this.values == null || !this.values.equals(other.values))) {
            return false;
        }
        if (this.not != other.not) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        if (not) {
            b.append("not ");
        }
        b.append("in").append('(');
        b.append(prop).append(',');
        for (int i = 0; i < values.size(); i++) {
            b.append(values.get(i));
            b.append(',');
        }
        b.setCharAt(b.length() - 1, ')');
        return b.toString();
    }

}