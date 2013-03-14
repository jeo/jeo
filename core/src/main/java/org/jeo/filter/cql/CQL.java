package org.jeo.filter.cql;

import org.jeo.filter.Filter;

/**
 * CQL utility class.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class CQL {

    public static Filter parse(String cql) throws ParseException {
        return new CQLBuilder(cql).parse();
    }
}
