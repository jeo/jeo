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
        catch (Throwable orig) {
            try {
                return new ECQLBuilder(cql).parse();
            }
            catch (Throwable e) {
                if (e instanceof ParseException) {
                    throw (ParseException) e;
                }
                if (e instanceof TokenMgrError) {
                    throw (ParseException) new ParseException("Invalid CQL syntax: " + e.getMessage()).initCause(e);
                }
                throw (ParseException) new ParseException("CQL Parsing error").initCause(e);
            }
        }

    }
}
