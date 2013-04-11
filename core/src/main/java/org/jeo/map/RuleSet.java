package org.jeo.map;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RuleSet implements Iterable<Rule>{

    List<Rule> rules;

    public RuleSet(List<Rule> rules) {
        this.rules = rules;
    }

    public boolean isEmpty() {
        return rules.isEmpty();
    }

    public Rule first() {
        return !rules.isEmpty() ? rules.iterator().next() : null;
    }

    public List<Rule> list() {
        return rules;
    }

    @Override
    public Iterator<Rule> iterator() {
        return list().iterator();
    }

    public RuleSet match(Object obj) {
        List<Rule> match = new ArrayList<Rule>();
        O: for (Rule r : rules) {
            for (Selector s : r.getSelectors()) {
                if (s.getFilter() == null || s.getFilter().apply(obj)) {
                    match.add(r);
                    continue O;
                }
            }
        }

        return new RuleSet(match);
    }

    public Rule collapse() {
        Rule rule = new Rule();
        for (Rule r : rules) {
            rule = rule.merge(r);
        }
        return rule;
    }
}
