package org.jeo.filter;

/**
 * Derives a value from a given input.
 *  
 * @author Justin Deoliveira, OpenGeo
 */
public interface Expression {

    /**
     * Returns the value of the given expression for the specified input.
     */
    Object evaluate(Object obj);
}
