package org.jeo.map;

public interface RuleVisitor {

    boolean visit(Rule rule, Stylesheet stylesheet);
}
