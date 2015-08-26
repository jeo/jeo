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
 * Filter adapter that returns Boolean from each visit method and stubs out all
 * calls returning false.
 */
public class BooleanFilterAdapter implements FilterVisitor<Boolean> {

    @Override
    public Boolean visit(Self self, Object obj) {
        return false;
    }

    @Override
    public Boolean visit(Literal literal, Object obj) {
        return false;
    }

    @Override
    public Boolean visit(Property property, Object obj) {
        return false;
    }

    @Override
    public Boolean visit(Function function, Object obj) {
        return false;
    }

    @Override
    public Boolean visit(Mixed mixed, Object obj) {
        return false;
    }

    @Override
    public Boolean visit(Math math, Object obj) {
        return false;
    }

    @Override
    public Boolean visit(Expression expr, Object obj) {
        return false;
    }

    @Override
    public Boolean visit(All<?> all, Object obj) {
        return false;
    }

    @Override
    public Boolean visit(None<?> none, Object obj) {
        return false;
    }

    @Override
    public Boolean visit(Id<?> id, Object obj) {
        return false;
    }

    @Override
    public Boolean visit(Logic<?> logic, Object obj) {
        return false;
    }

    @Override
    public Boolean visit(Comparison<?> compare, Object obj) {
        return false;
    }

    @Override
    public Boolean visit(Spatial<?> spatial, Object obj) {
        return false;
    }

    @Override
    public Boolean visit(TypeOf<?> inst, Object obj) {
        return false;
    }

    @Override
    public Boolean visit(In<?> in, Object obj) {
        return false;
    }

    @Override
    public Boolean visit(Like<?> like, Object obj) {
        return false;
    }

    @Override
    public Boolean visit(Null<?> isNull, Object obj) {
        return false;
    }

    @Override
    public Boolean visit(Filter<?> filter, Object obj) {
        return false;
    }
}
