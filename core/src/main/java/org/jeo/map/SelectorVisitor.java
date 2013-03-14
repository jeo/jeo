package org.jeo.map;

public interface SelectorVisitor {

    boolean visit(Selector selector, Rule rule, Stylesheet style);
}
