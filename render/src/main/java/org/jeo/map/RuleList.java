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
package org.jeo.map;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.jeo.data.Query;
import org.jeo.feature.Feature;
import org.jeo.filter.Expression;
import org.jeo.filter.Filter;
import org.jeo.filter.Filters;

/**
 * List of rules providing methods for processing and transforming rules in the list.
 * <p>
 * All the methods in this class that transform the rule list create a new rule set, leaving the
 * original unmodified.
 * </p>
 * @author Justin Deoliveira, OpenGeo
 */
public class RuleList extends ArrayList<Rule> {

    /** serialVersionUID */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs an empty rule list.
     */
    public RuleList() {
    }

    /**
     * Constructs a rule list from an existing list.
     */
    public RuleList(List<Rule> rules) {
        super(rules);
    }

    /**
     * The first rule, or <code>null</code> if the list is empty.
     */
    public Rule first() {
        return !isEmpty() ? iterator().next() : null;
    }

    /**
     * Collapses the rule list into a single rule. 
     * 
     * @see Rule#merge(Rule)
     */
    public Rule collapse() {
        Rule rule = new Rule();
        for (Rule r : this) {
            rule = rule.merge(r);
        }
        return rule;
    }

    /**
     * Returns a new rule list containing rules that pass the criteria of the specified rule 
     * visitor. 
     */
    public RuleList select(RuleVisitor visitor) {
        RuleList match = new RuleList();
        for (Rule r : this) {
            if (visitor.visit(r)) {
                match.add(r);
            }
        }
        return match;
    }

    /**
     * Returns a new rule list containing rules that pass the criteria of the specified selector 
     * visitor. 
     */
    public RuleList select(SelectorVisitor visitor) {
        RuleList match = new RuleList();
        O: for (Rule r : this) {
            for (Selector s : r.getSelectors()) {
                if (visitor.visit(s, r)) {
                    match.add(r);
                    continue O;
                }
            }
        }
        return match;
    }

    /**
     * Returns a new rule list containing rules that have a selector whose id matches the specified
     * <tt>id</tt>.
     * <p>
     * If <tt>wildcard</tt> is <code>true</code> the result includes rules containing a wildcard
     * selector.
     * </p>
     */
    public RuleList selectById(final String id, final boolean wildcard) {
        return select(new SelectorVisitor() {
            @Override
            public boolean visit(Selector selector, Rule rule) {
                if (wildcard && selector.isWildcard()) {
                    return true;
                }

                return id == null && selector.getId() == null || id.equals(selector.getId());
            }
        });
    }

    /**
     * Returns a new rule list containing rules that have a selector whose name matches the 
     * specified <tt>name</tt>.
     * <p>
     * If <tt>wildcard</tt> is <code>true</code> the result includes rules containing a wildcard
     * selector. The <tt>matchCase</tt> argument controls case sensitivity of the name match.
     * </p>
     */
    public RuleList selectByName(final String name, final boolean wildcard, final boolean matchCase) {
        return select(new SelectorVisitor() {
            @Override
            public boolean visit(Selector selector, Rule rule) {
                if (wildcard && selector.isWildcard()) {
                    return true;
                }

                String selName = selector.getName();
                return (name == null && selName == null) || 
                    (matchCase ? name.equals(selName) : name.equalsIgnoreCase(selName));
            }
        });
    }

    /**
     * Returns a new rule list containing rules that have a selector whose filter matches the 
     * specified <tt>feature</tt>.
     * <p>
     * Rules that specify no filter are included in the result.
     * </p>
     */
    public RuleList match(Feature feature) {
        List<Rule> match = new ArrayList<Rule>();
        O: for (Rule r : this) {
            for (Selector s : r.getSelectors()) {
                if (s.getFilter() == null || s.getFilter().apply(feature)) {
                    match.add(r);
                    continue O;
                }
            }
        }

        return new RuleList(match);
    }

    /**
     * Returns a new rule list consisting of all the rules in this list flattened. 
     *  
     * @see Rule#flatten()
     */
    public RuleList flatten() {
        RuleList flat = new RuleList();
        for (Rule r : this) {
            flat.addAll(r.flatten());
        }
        return flat;
    }

    /**
     * Returns a list of rule lists grouped by z (attachment) order.
     */
    public List<RuleList> zgroup() {
        LinkedHashMap<String, List<Rule>> z = new LinkedHashMap<String, List<Rule>>();
        for (Rule r : this) {
            String att = null;
            for (Iterator<Selector> it = r.getSelectors().iterator(); it.hasNext() && att == null;) {
                att = it.next().getAttachment();
            }

            List<Rule> list = z.get(att);
            if (list == null) {
                list = new ArrayList<Rule>();
                z.put(att, list);
            }
            list.add(r);
        }

        List<RuleList> grouped = new ArrayList<RuleList>();
        for (List<Rule> l : z.values()) {
            grouped.add(new RuleList(l));
        }
        return grouped;
    }

    /**
     * Return a set of fields used by the rules in this list.
     *
     * @return Set containing the fields
     */
    public Set<String> fields() {
        Set<String> fields = new LinkedHashSet<String>();
        for (Rule r : this) {
            fields(r, fields);
        }
        return fields;
    }

    void fields(Rule r, Set<String> fields) {
        for (Selector s : r.getSelectors()) {
            if (s.getFilter() != null) {
                Filters.properties(s.getFilter(), fields);
            }
        }
        for (Object val : r.properties().values()) {
            if (val instanceof Expression) {
                Filters.properties((Expression) val, fields);
            }
        }

        for (Rule n : r.nested()) {
            fields(n, fields);
        }
    }
}
