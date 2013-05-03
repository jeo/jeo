package org.jeo.data;

import java.util.Iterator;

/**
 * Interface for registry for drivers. 
 * <p>
 * Typically the registry is not used directly to query for drivers, the {@link Drivers} class is
 * used for that purpose.
 * </p>
 * @author Justin Deoliveira, OpenGeo
 */
public interface DriverRegistry {

    /**
     * Lists all the drivers in this registry.
     */
    Iterator<Driver<?>> list();
}
