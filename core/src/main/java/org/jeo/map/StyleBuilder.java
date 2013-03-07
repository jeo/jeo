package org.jeo.map;

import java.util.List;

import org.jeo.feature.Feature;
import org.jeo.filter.Filter;

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

    public StyleBuilder filter(Filter<Feature> filter) {
        lastRule().setFilter(filter);
        return this;
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
}
