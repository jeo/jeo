package org.jeo.map;

/**
 * Visitor for {@link Selector} objects. 
 *  
 * @author Justin Deoliveira, OpenGeo
 */
public interface SelectorVisitor {

    /**
     * Tests some criteria against the specified <tt>selector</tt>
     * 
     * @param selector The selector to test.
     * @param rule The parent rule of the selector. 
     * 
     * @return <code>true</code> if the selector passes the criteria.
     */
    boolean visit(Selector selector, Rule rule);
}
