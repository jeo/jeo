package org.jeo.data;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Driver registry that uses a static list of driver objects.
 * 
 * @author Justin Deoliveira, Boundless
 */
public class StaticDriverRegistry implements DriverRegistry {

    List<Driver<?>> drivers;

    public StaticDriverRegistry(Driver<?>... drivers) {
        this.drivers = Arrays.asList(drivers);
    }

    @Override
    public Iterator<Driver<?>> list() {
        return drivers.iterator();
    }
}
