package org.jeo.cql;

import org.jeo.filter.Filter;

/**
 * CQL utility class.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class CQL {

    public static Filter<Object> parse(String cql) throws ParseException {
        return new CQLBuilder(cql).parse();
    }
}
