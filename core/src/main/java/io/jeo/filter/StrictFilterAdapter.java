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

import java.lang.*;

/**
 * Adapter for {@link FilterVisitor} that stubs each method with a {@link java.lang.UnsupportedOperationException}.
 */
public class StrictFilterAdapter<T> implements FilterVisitor<T> {

    @Override
    public T visit(Self self, Object obj) {
        throw new UnsupportedOperationException("Self expression not supported");
    }

    @Override
    public T visit(Literal literal, Object obj) {
        throw new UnsupportedOperationException("Literal expression not supported");
    }

    @Override
    public T visit(Property property, Object obj) {
        throw new UnsupportedOperationException("Property expression not supported");
    }

    @Override
    public T visit(Function function, Object obj) {
        throw new UnsupportedOperationException("Function expression not supported");
    }

    @Override
    public T visit(Mixed mixed, Object obj) {
        throw new UnsupportedOperationException("Mixed expression not supported");
    }

    @Override
    public T visit(Expression expr, Object obj) {
        throw new UnsupportedOperationException("Expression not supported");
    }

    @Override
    public T visit(All<?> all, Object obj) {
        throw new UnsupportedOperationException("All filter not supported");
    }

    @Override
    public T visit(None<?> none, Object obj) {
        throw new UnsupportedOperationException("None filter not supported");
    }

    @Override
    public T visit(Id<?> id, Object obj) {
        throw new UnsupportedOperationException("Id filter not supported");
    }

    @Override
    public T visit(Logic<?> logic, Object obj) {
        throw new UnsupportedOperationException("Logic filter not supported");
    }

    @Override
    public T visit(Comparison<?> compare, Object obj) {
        throw new UnsupportedOperationException("Comparison filter not supported");
    }

    @Override
    public T visit(Spatial<?> spatial, Object obj) {
        throw new UnsupportedOperationException("Spatial filter not supported");
    }

    @Override
    public T visit(TypeOf<?> inst, Object obj) {
        throw new UnsupportedOperationException("TypeOf filter not supported");
    }

    @Override
    public T visit(In<?> in, Object obj) {
        throw new UnsupportedOperationException("In filter not supported");
    }

    @Override
    public T visit(Like<?> like, Object obj) {
        throw new UnsupportedOperationException("Like filter not supported");
    }

    @Override
    public T visit(Math math, Object obj) {
        throw new UnsupportedOperationException("Math filter not supported");
    }

    @Override
    public T visit(Null<?> isNull, Object obj) {
        throw new UnsupportedOperationException("Null filter not supported");
    }

    @Override
    public T visit(Filter<?> filter, Object obj) {
        throw new UnsupportedOperationException("Filter not supported");
    }
}
