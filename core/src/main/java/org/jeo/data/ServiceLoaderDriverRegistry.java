package org.jeo.data;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Loads drivers with {@link ServiceLoader}.
 * <p>
 * This registry picks up drivers registered via the file META-INF/services/org.jeo.data.Driver.
 * </p>
 *
 * @author Justin Deoliveira, OpenGeo
 */
public class ServiceLoaderDriverRegistry implements DriverRegistry {

    @Override
    public Iterator<Driver<?>> list() {
        return (Iterator) ServiceLoader.load(Driver.class).iterator();
    }

}
