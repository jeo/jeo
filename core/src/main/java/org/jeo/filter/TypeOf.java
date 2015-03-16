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

import java.util.Objects;

/**
 * Filter that tests the type of an object.
 * 
 * @author Justin Deoliveira, Boundless
 */
public class TypeOf<T> extends Filter<T> {

    Expression expr;
    Class<?> type;

    public TypeOf(Expression expr, Class<?> type) {
        Objects.requireNonNull(expr, "expression must not be null");
        Objects.requireNonNull(type, "type must not be null");
        this.expr = expr;
        this.type = type;
    }

    public Class<?> type() {
        return type;
    }

    public Expression expression() {
        return expr;
    }

    @Override
    public boolean test(Object obj) {
        obj = expr.evaluate(obj);

        if (obj instanceof Class) {
            return type.isAssignableFrom((Class)obj);
        }
        return type.isInstance(obj);
    }

    @Override
    public <R> R accept(FilterVisitor<R> v, Object obj) {
        return v.visit(this, obj);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TypeOf typeOf = (TypeOf) o;

        if (!expr.equals(typeOf.expr)) return false;
        if (!type.equals(typeOf.type)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = expr.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }
}
