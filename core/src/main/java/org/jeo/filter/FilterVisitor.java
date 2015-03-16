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

/**
 * Visitor for filter classes.
 * <p>
 * Each visit method takes an instance of either {@link Expression} or {@link Filter}, as well as a
 * an additional object as a "hint" or "context". This object is usually used by the filter visitor
 * itself to pass state around. It is often null.
 * </p>
 * 
 * @author Justin Deoliveira, Boundless
 */
public interface FilterVisitor<T> {

    /**
     * Visits a Self expression.
     */
    T visit(Self self, Object obj);

    /**
     * Visits a Literal expression.
     */
    T visit(Literal literal, Object obj);

    /**
     * Visits a Property expression.
     */
    T visit(Property property, Object obj);

    /**
     * Visits a Function expression.
     */
    T visit(Function function, Object obj);

    /**
     * Visits a Mixed expression.
     */
    T visit(Mixed mixed, Object obj);

    /**
     * Visits a Math expression.
     */
    T visit(Math math, Object obj);

    /**
     * Visits an expression.
     */
    T visit(Expression expr, Object obj);

    /**
     * Visits an All filter.
     */
    T visit(All<?> all, Object obj);

    /**
     * Visits a None filter.
     */
    T visit(None<?> none, Object obj);

    /**
     * Visits a Id filter.
     */
    T visit(Id<?> id, Object obj);

    /**
     * Visits a Logic filter.
     */
    T visit(Logic<?> logic, Object obj);

    /**
     * Visits a Comparison filter.
     */
    T visit(Comparison<?> compare, Object obj);

    /**
     * Visits a Spatial filter.
     */
    T visit(Spatial<?> spatial, Object obj);

    /**
     * Visits a Logic filter.
     */
    T visit(TypeOf<?> inst, Object obj);

    /**
     * Visits an In filter.
     */
    T visit(In<?> in, Object obj);

    /**
     * Visits a Like filter.
     */
    T visit(Like<?> like, Object obj);

    /**
     * Visits a Null filter.
     */
    T visit(Null<?> isNull, Object obj);

    /**
     * Visits a filter.
     */
    T visit(Filter<?> filter, Object obj);
}
