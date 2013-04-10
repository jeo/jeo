package org.jeo.map;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines the rules used to symbolize a map.
 * <p>
 * 
 * </p> 
 * @author Justin Deoliveira, OpenGeo
 */
public class Stylesheet {

    List<Rule> rules = new ArrayList<Rule>();

    /**
     * The rules making up the stylesheet.
     */
    public List<Rule> getRules() {
        return rules;
    }

    public List<Rule> select(RuleVisitor visitor) {
        List<Rule> match = new ArrayList<Rule>();
        for (Rule r : rules) {
            if (visitor.visit(r, this)) {
                match.add(r);
            }
        }
        return match;
    }

    public RuleSet select(SelectorVisitor visitor) {
        List<Rule> match = new ArrayList<Rule>();
        O: for (Rule r : rules) {
            for (Selector s : r.getSelectors()) {
                if (visitor.visit(s, r, this)) {
                    match.add(r);
                    continue O;
                }
            }
        }
        return new RuleSet(match);
    }

    public RuleSet selectById(final String id, final boolean wildcard) {
        return select(new SelectorVisitor() {
            @Override
            public boolean visit(Selector selector, Rule rule, Stylesheet style) {
                if (wildcard && selector.isWildcard()) {
                    return true;
                }

                return id == null && selector.getId() == null || id.equals(selector.getId());
            }
        });
    }
   
    public RuleSet selectByName(final String name) {
        return select(new SelectorVisitor() {
            @Override
            public boolean visit(Selector selector, Rule rule, Stylesheet style) {
                return name == null && selector.getName() == null || name.equals(selector.getName());
            }
        });
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Rule r : rules) {
            sb.append(r).append("\n");
        }
        return sb.toString();
    }
}
