package org.jeo.data;

import java.io.IOException;
import java.util.Map;

/**
 * Format driver.
 * <p>
 * The job of a driver is to create a "connection" to data in a particular format. A driver can 
 * return either one of a dataset, or a workspace containing many datasets. The data 
 * </p>
 * <p>
 * Driver implementations must adhere to the following guidelines:
 * <ul>
 *  <li>Be thread safe and ideally maintain no state</li>
 *  <li>Contain a single no argument constructor</li>
 * </ul>
 * </p>
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public interface Driver<T> {

    /**
     * Name identifying the driver.
     * <p>
     * This name should be no more than a few words (ideally one) and isn't meant to 
     * be a description.  
     * </p>
     */
    String getName();

   /**
     * Returns the class of object returned by the driver.
     */
    Class<T> getType();

    /**
     * Determines if this driver can open a connection to the data described by the specified
     * options.
     *  
     * @param opts Options describing the data.
     * 
     * @return True if the driver can open the data, otherwise false.
     */
    boolean canOpen(Map<?,Object> opts);

    /**
     * Opens a connection to data described by the specified options.
     * 
     * @param opts Options describing the data to connect to.
     * 
     * @return The data.
     * 
     * @throws IOException In the event of a connection error such as a file system error or 
     *   database connection failure. 
     */
    T open(Map<?,Object> opts) throws IOException;
}
