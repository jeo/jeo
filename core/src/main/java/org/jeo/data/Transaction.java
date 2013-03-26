package org.jeo.data;

import java.io.IOException;

/**
 * Transaction object.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public interface Transaction {

    /**
     * Commits the transaction.
     */
    void commit() throws IOException;

    /**
     * Rolls back the transaction.
     */
    void rollback() throws IOException;
}
