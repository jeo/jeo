package org.jeo.data;

import java.util.Map;

/**
 * Interface implemented by Datasets that support transactions. 
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public interface Transactional {

    /**
     * Creates a new transaction object.
     * 
     * @param options Implementation options to use when creating the transaction, may be 
     * <code>null</code>.
     * 
     * @return A new transaction instance.
     */
    Transaction transaction(Map<String,Object> options);
}
