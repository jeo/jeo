package org.jeo.filter.cql;

import org.jeo.filter.Filter;

/**
 * CQL utility class.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class CQL {

    public static Filter parse(String cql) throws ParseException {
        try {
            return new CQLBuilder(cql).parse();
        }
        catch(Exception orig) {
            try {
                return new ECQLBuilder(cql).parse();
            }
            catch(ParseException e) {
                if (orig instanceof ParseException) {
                    throw (ParseException) orig;
                }
                throw (ParseException) new ParseException("Parsing error").initCause(orig);
            }
        }

    }
}
