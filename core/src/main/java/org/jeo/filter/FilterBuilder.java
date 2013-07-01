package org.jeo.filter;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.jeo.filter.Logic.Type;

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

        stack.push(new Comparison(Comparison.Type.GREATER_OR_EQUAL, e, low).
            and(new Comparison(Comparison.Type.LESS_OR_EQUAL, e, high)));
        return this;
    }

    public FilterBuilder id() {
        List<Expression> ids = new ArrayList<Expression>();
        while(!stack.isEmpty() && stack.peek() instanceof Expression) {
            ids.add((Expression) stack.pop());
        }
        stack.push(new Id(ids));
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

    public Filter filter() {
        return (Filter) stack.pop();
    }

    FilterBuilder cmp(Comparison.Type type) {
        Expression e2 = (Expression) stack.pop();
        Expression e1 = (Expression) stack.pop();
        stack.push(new Comparison(type, e1, e2));
        return this;
    }

    FilterBuilder log(Logic.Type type) {
        LinkedList<Filter> parts = new LinkedList<Filter>();

        while(stack.peek() instanceof Filter) {
            parts.addFirst((Filter)stack.pop());
            if (type == Type.NOT) {
                break;
            }
        }

        stack.push(new Logic(type, parts));
        return this;
    }

    FilterBuilder spatial(Spatial.Type type) {
        Expression e2 = (Expression) stack.pop();
        Expression e1 = (Expression) stack.pop();
        stack.push(new Spatial(type, e1, e2));
        return this;
    }
}
