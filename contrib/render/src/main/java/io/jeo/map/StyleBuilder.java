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
package io.jeo.map;

import java.util.ArrayDeque;
import java.util.Deque;

import io.jeo.filter.Filter;
import io.jeo.filter.cql.CQL;
import io.jeo.filter.cql.ParseException;

public class StyleBuilder {

    Deque<Rule> ruleStack = new ArrayDeque<Rule>();
    Style style = new Style();

    public StyleBuilder set(String key, Object value) {
//        if (style.getRules().isEmpty()) {
//            style.put(key, value);
//        }
//        else {
            lastRule().put(key, value);
//        }
        return this;
    }

    public StyleBuilder rule() {
        return rule(new Rule());
    }

    public StyleBuilder rule(Rule r) {
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

    public StyleBuilder select(Selector selector) {
        lastRule().getSelectors().add(selector);
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

    public Style style() {
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
