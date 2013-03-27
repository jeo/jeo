package org.jeo.map;

import java.util.ArrayDeque;
import java.util.Deque;

import org.jeo.filter.Filter;
import org.jeo.filter.cql.CQL;
import org.jeo.filter.cql.ParseException;

public class StyleBuilder {

    Deque<Rule> ruleStack = new ArrayDeque<Rule>();
    Stylesheet style = new Stylesheet();

    public StyleBuilder set(String key, Object value) {
        if (style.getRules().isEmpty()) {
            style.put(key, value);
        }
        else {
            lastRule().put(key, value);
        }
        return this;
    }

    public StyleBuilder rule() {
        Rule r = new Rule();
        if (ruleStack.isEmpty()) {
            style.getRules().add(r);
        }
        else {
            ruleStack.peek().add(r);
        }
    
        ruleStack.push(r);
        return this;
    }

    public StyleBuilder endRule() {
        ruleStack.pop();
        return this;
    }

    public StyleBuilder select(String name) {
        Selector s = lastSelector();
        if (name != null) {
            if (name.equals("*")) {
                s.setWildcard(true);
            }
            else if (name.startsWith("#")) {
                s.setId(name.substring(1));
            }
            else if (name.startsWith(".")) {
                s.getClasses().add(name.substring(1));
            }
            else if (name.startsWith("::")) {
                s.setAttachment(name.substring(2));
            }
            else {
                s.setName(name);
            }
        }
        return this;
    }

    public StyleBuilder filter(Filter filter) {
        lastSelector().setFilter(filter);
        return this;
    }

    public StyleBuilder filter(String cql) {
        try {
            return filter(CQL.parse(cql));
        } catch (ParseException e) {
            throw new IllegalArgumentException(cql, e);
        }
    }

    public Stylesheet style() {
        return style;
    }

    Rule lastRule() {
        if (ruleStack.isEmpty()) {
            rule();
            //throw new IllegalStateException(
            //        "No rule on the stack, call the rule() method before this method");
        }
        return ruleStack.peek();
    }

    Selector lastSelector() {
        Rule r = lastRule();
        if (r.getSelectors().isEmpty()) {
            r.getSelectors().add(new Selector());
            //throw new IllegalStateException(
            //    "No selector on the stack, call the select() method before this method");
        }
        return r.getSelectors().get(r.getSelectors().size()-1);
    }
}
