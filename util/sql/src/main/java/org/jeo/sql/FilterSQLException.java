package org.jeo.sql;

import org.jeo.filter.Filter;

/**
 * Exception thrown when {@link FilterSQLEncoder} is unable to encode a {@link Filter} as SQL. 
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class FilterSQLException extends RuntimeException {

    /** serialVersionUID */
    private static final long serialVersionUID = 1L;

    public FilterSQLException(String message) {
        super(message);
    }

    public FilterSQLException(String message, Throwable cause) {
        super(message, cause);
    }
}
