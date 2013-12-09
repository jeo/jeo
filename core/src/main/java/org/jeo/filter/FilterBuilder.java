package org.jeo.filter;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

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
        stack.push(new Literal(o));
        return this;
    }

    public FilterBuilder property(String prop) {
        stack.push(new Property(prop));
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

    public FilterBuilder between() {
        Expression high = (Expression) stack.pop();
        Expression low = (Expression) stack.pop();
        Expression e = (Expression) stack.pop();

        stack.push(new Comparison<Object>(Comparison.Type.GREATER_OR_EQUAL, e, low).
            and(new Comparison<Object>(Comparison.Type.LESS_OR_EQUAL, e, high)));
        return this;
    }

    public FilterBuilder id() {
        List<Expression> ids = new ArrayList<Expression>();
        while(!stack.isEmpty() && stack.peek() instanceof Expression) {
            ids.add((Expression) stack.pop());
        }
        stack.push(new Id<Object>(ids));
        return this;
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

    public FilterBuilder touch() {
        return spatial(Spatial.Type.TOUCH);
    }

    public FilterBuilder disjoint() {
        return spatial(Spatial.Type.DISJOINT);
    }

    public FilterBuilder overlap() {
        return spatial(Spatial.Type.OVERLAP);
    }

    public FilterBuilder cross() {
        return spatial(Spatial.Type.CROSS);
    }

    public FilterBuilder cover() {
        return spatial(Spatial.Type.COVER);
    }

    public FilterBuilder within() {
        return spatial(Spatial.Type.WITHIN);
    }

    public FilterBuilder contain() {
        return spatial(Spatial.Type.CONTAIN);
    }

    public FilterBuilder bbox() {
        return spatial(Spatial.Type.BBOX);
    }

    public Object pop() {
        return stack.pop();
    }

    public Filter filter() {
        return (Filter) stack.pop();
    }

    public FilterBuilder type(Class<?> type) {
        if (stack.peek() instanceof Expression) {
            stack.push(new TypeOf<Object>((Expression)stack.pop(), type));
        }
        else {
            stack.push(new TypeOf<Object>(new Self(), type));
        }
        return this;
    }

    public <T> FilterBuilder cmp(Comparison.Type type) {
        Expression e2 = (Expression) stack.pop();
        Expression e1 = (Expression) stack.pop();
        stack.push(new Comparison<Object>(type, e1, e2));
        return this;
    }

    FilterBuilder log(Logic.Type type) {
        LinkedList<Filter<Object>> parts = new LinkedList<Filter<Object>>();

        while(stack.peek() instanceof Filter) {
            parts.addFirst((Filter<Object>)stack.pop());
            if (type == Logic.Type.NOT) {
                break;
            }
        }

        stack.push(new Logic<Object>(type, parts));
        return this;
    }

    FilterBuilder spatial(Spatial.Type type) {
        Expression e2 = (Expression) stack.pop();
        Expression e1 = (Expression) stack.pop();
        stack.push(new Spatial<Object>(type, e1, e2));
        return this;
    }
}
