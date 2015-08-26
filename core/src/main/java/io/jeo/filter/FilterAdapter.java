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
package io.jeo.filter;

/**
 * Adapter for {@link FilterVisitor} that stubs all methods.
 */
public class FilterAdapter<T> implements FilterVisitor<T> {

    @Override
    public T visit(Self self, Object obj) {
        return null;
    }

    @Override
    public T visit(Literal literal, Object obj) {
        return null;
    }

    @Override
    public T visit(Property property, Object obj) {
        return null;
    }

    @Override
    public T visit(Function function, Object obj) {
        return null;
    }

    @Override
    public T visit(Mixed mixed, Object obj) {
        return null;
    }

    @Override
    public T visit(Math math, Object obj) {
        return null;
    }

    @Override
    public T visit(Expression expr, Object obj) {
        return null;
    }

    @Override
    public T visit(All<?> all, Object obj) {
        return null;
    }

    @Override
    public T visit(None<?> none, Object obj) {
        return null;
    }

    @Override
    public T visit(Id<?> id, Object obj) {
        return null;
    }

    @Override
    public T visit(Logic<?> logic, Object obj) {
        return null;
    }

    @Override
    public T visit(Comparison<?> compare, Object obj) {
        return null;
    }

    @Override
    public T visit(Spatial<?> spatial, Object obj) {
        return null;
    }

    @Override
    public T visit(TypeOf<?> inst, Object obj) {
        return null;
    }

    @Override
    public T visit(In<?> in, Object obj) {
        return null;
    }

    @Override
    public T visit(Like<?> like, Object obj) {
        return null;
    }

    @Override
    public T visit(Null<?> isNull, Object obj) {
        return null;
    }

    @Override
    public T visit(Filter<?> filter, Object obj) {
        return null;
    }
}
