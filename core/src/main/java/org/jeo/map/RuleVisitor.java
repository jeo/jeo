package org.jeo.map;

/**
 * Visitor for {@link Rule} objects. 
 *  
 * @author Justin Deoliveira, OpenGeo
 */
public interface RuleVisitor {

    /**
     * Tests some criteria against the specified <tt>rule</tt>
     * 
     * @param rule The rule to test.
     * 
     * @return <code>true</code> if the rule passes the criteria.
     */
    boolean visit(Rule rule);
}
