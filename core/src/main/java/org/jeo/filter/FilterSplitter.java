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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expassss or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jeo.filter;

import org.jeo.filter.Logic.Type;
import org.jeo.util.Pair;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Splits a filter into two parts based on criteria provided by a filter visitor.
 * <p>
 * The first part of the filter contains the conjunction of all filters matching the criteria. The second
 * part of the filter contains those parts that fail the criteria.
 * </p>
 * <p>
 * For logic filters the following rules are used to split.
 * <ol>
 *   <li>If all of the children exclusively pass or exclusively fail, the filter as a whole is added to the pass/fail
 *   set respectively</li>
 *   <li>If the filter is a conjunction (AND) the children are split into the pass/fail sets as appropriate.</li>
 * </p>
 */
public class FilterSplitter extends StrictFilterAdapter<Object> {

    FilterVisitor<Boolean> qualifier;

    public FilterSplitter(FilterVisitor<Boolean> qualifier) {
        this.qualifier = qualifier;
    }

    public Pair<Filter,Filter> split(Filter filter) {
        FilterStack stack = new FilterStack();
        filter.accept(this, stack);

        Filter pass = Filters.all();
        while (!stack.pass.isEmpty()) {
            pass = stack.pass.pop().and(pass);
        }

        Filter fail = Filters.all();
        while (!stack.fail.isEmpty()) {
            fail = stack.fail.pop().and(fail);
        }

        return Pair.of(pass, fail);
    }

    @Override
    public Object visit(Self self, Object obj) {
        return null;
    }

    @Override
    public Object visit(Literal literal, Object obj) {
        return null;
    }

    @Override
    public Object visit(Property property, Object obj) {
        return null;
    }

    @Override
    public Object visit(Function function, Object obj) {
        return null;
    }

    @Override
    public Object visit(Mixed mixed, Object obj) {
        return null;
    }

    @Override
    public Object visit(Math math, Object obj) {
        return null;
    }

    @Override
    public Object visit(Expression expr, Object obj) {
        return null;
    }

    @Override
    public Object visit(All<?> all, Object obj) {
        return test(all, obj);
    }

    @Override
    public Object visit(None<?> none, Object obj) {
        return test(none, obj);
    }

    @Override
    public Object visit(Id<?> id, Object obj) {
        return test(id, obj);
    }

    @Override
    public Object visit(Comparison<?> compare, Object obj) {
        return test(compare, obj);
    }

    @Override
    public Object visit(Spatial<?> spatial, Object obj) {
        return test(spatial, obj);
    }

    @Override
    public Object visit(TypeOf<?> inst, Object obj) {
        return test(inst, obj);
    }

    @Override
    public Object visit(In<?> in, Object obj) {
        return test(in, obj);
    }

    @Override
    public Object visit(Like<?> like, Object obj) {
        return test(like, obj);
    }

    @Override
    public Object visit(Null<?> isNull, Object obj) {
        return test(isNull, obj);
    }

    @Override
    public Object visit(Filter<?> filter, Object obj) {
        return test(filter, obj);
    }

    @Override
    public Object visit(Logic<?> logic, Object obj) {
        FilterStack stack = (FilterStack) obj;
        int pass = stack.pass.size();
        int fail = stack.fail.size();

        for (Filter f : logic.parts()) {
            test(f, obj);
        }

        if (stack.pass.size() == pass && stack.fail.size() == fail ) {
            throw new IllegalStateException("no change in stack");
        }

        if (stack.fail.size() > fail) {
            // unhandled filters, unless the filter is an AND, abort
            if (logic.type() != Type.AND) {
                popUntil(stack.pass, pass);
                popUntil(stack.fail, fail);
                stack.fail.push(logic);
            }
        }
        else {
            // all handlable, pull off stack and replace with this entire filter
            popUntil(stack.pass, pass);
            stack.pass.push(logic);
        }

        return obj;
    }

    Object test(Filter f, Object obj) {
        FilterStack stack = (FilterStack) obj;
        if (f instanceof Logic) {
            f.accept(this, obj);
        }
        else {
            Boolean handle = (Boolean) f.accept(qualifier, obj);
            if (handle) {
                stack.pass.push(f);
            }
            else{
                stack.fail.push(f);
            }
        }

        return obj;
    }

    void popUntil(Deque<Filter> stack, int n) {
        while (stack.size() > n) {
            stack.pop();
        }
    }

    class FilterStack {
        Deque<Filter> pass = new ArrayDeque<>();
        Deque<Filter> fail = new ArrayDeque<>();
    }
}
