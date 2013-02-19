package org.jeo.filter;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;

/**
 * Builder for filters that can compose complex filter / expression trees. 
 * <p>
 * Example usage:
 * <pre>
 * Filter f = new FilterBuilder()
 *    .property("foo").literal(10).gt()
 *    .property("bar").literal("blah").eq()
 *    .or();
 * </pre>
 * Would result in the filter <tt>[foo] > 10 OR [bar] = "blah"</tt>
 * </p> 
 * @author Justin Deoliveira, OpenGeo
 */
public class FilterBuilder {

    Deque<Object> stack = new ArrayDeque<Object>();

    public FilterBuilder() {
    }

    public FilterBuilder literal(Object o) {
        stack.push(new Literal<Object>(o));
        return this;
    }

    public FilterBuilder property(String prop) {
        stack.push(new Property<Object>(prop));
        return this;
    }

    public FilterBuilder eq() {
        return cmp(Comparison.Type.EQUAL);
    }

    public FilterBuilder neq() {
        return cmp(Comparison.Type.NOT_EQUAL);
    }

    public FilterBuilder lt() {
        return cmp(Comparison.Type.LESS);
    }

    public FilterBuilder lte() {
        return cmp(Comparison.Type.LESS_OR_EQUAL);
    }

    public FilterBuilder gt() {
        return cmp(Comparison.Type.GREATER);
    }

    public FilterBuilder gte() {
        return cmp(Comparison.Type.GREATER_OR_EQUAL);
    }

    public FilterBuilder and() {
        return log(Logic.Type.AND);
    }

    public FilterBuilder or() {
        return log(Logic.Type.OR);
    }

    public FilterBuilder not() {
        return log(Logic.Type.NOT);
    }

    public FilterBuilder intersect() {
        return spatial(Spatial.Type.INTERSECT);
    }
    @SuppressWarnings("unchecked")
    public Filter<Object> filter() {
        return (Filter<Object>) stack.pop();
    }

    @SuppressWarnings("unchecked")
    FilterBuilder cmp(Comparison.Type type) {
        Expression<Object> e2 = (Expression<Object>) stack.pop();
        Expression<Object> e1 = (Expression<Object>) stack.pop();
        stack.push(new Comparison<Object>(type, e1, e2));
        return this;
    }

    @SuppressWarnings("unchecked")
    FilterBuilder log(Logic.Type type) {
        LinkedList<Filter<Object>> parts = new LinkedList<Filter<Object>>();
        while(stack.peek() instanceof Filter) {
            parts.addFirst((Filter<Object>)stack.pop());
        }

        stack.push(new Logic<Object>(type, parts));
        return this;
    }

    @SuppressWarnings("unchecked")
    FilterBuilder spatial(Spatial.Type type) {
        Expression<Object> e2 = (Expression<Object>) stack.pop();
        Expression<Object> e1 = (Expression<Object>) stack.pop();
        stack.push(new Spatial<Object>(type, e1, e2));
        return this;
    }
}
