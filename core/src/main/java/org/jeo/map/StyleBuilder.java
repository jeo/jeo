package org.jeo.map;

import java.util.List;

import org.jeo.filter.Filter;
import org.jeo.filter.cql.CQL;
import org.jeo.filter.cql.ParseException;
import org.jeo.map.Selector.Type;

public class StyleBuilder {

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
        style.getRules().add(new Rule());
        return this;
    }

    public StyleBuilder select(String name) {
        Selector s = new Selector();
        if (name.startsWith("#")) {
            s.setType(Type.ID);
            s.setName(name.substring(1));
        }
        else if (name.startsWith(".")) {
            s.setType(Type.CLASS);
            s.setName(name.substring(1));
        }
        else {
            s.setType(Type.NAME);
            s.setName(name);
        }

        Rule r = lastRule();
        r.getSelectors().add(s);

        return this;
    }

    public StyleBuilder filter(Filter<?> filter) {
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
        List<Rule> rules = style.getRules();
        if (rules.isEmpty()) {
            throw new IllegalStateException(
                "No rule on the stack, call the rule() method before this method");
        }
        return rules.get(rules.size()-1);
    }

    Selector lastSelector() {
        Rule r = lastRule();
        if (r.getSelectors().isEmpty()) {
            throw new IllegalStateException(
                "No selector on the stack, call the select() method before this method");
        }
        return r.getSelectors().get(r.getSelectors().size()-1);
    }
}
