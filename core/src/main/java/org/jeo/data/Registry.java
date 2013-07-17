package org.jeo.data;

import java.io.IOException;

/**
 * A container for data objects.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public interface Registry extends Disposable {

    /**
     * The names of all items of the registry.
     */
    Iterable<Item> list();

    /**
     * Returns a registry object by name.
     * 
     * @param name The name of the object.
     * 
     * @return The object or <code>null</code> if so such object matching the name exists.
     */
    Object get(String name) throws IOException;

    /**
     * An object in the registry.
     */
    public static class Item {
        String name;
        Driver<?> driver;

        public Item(String name, Driver<?> driver) {
            this.name = name;
            this.driver = driver;
        }

        /**
         * Name of the registry object.
         */
        public String getName() {
            return name;
        }

        /**
         * The driver used to connect to the item.
         */
        public Driver<?> getDriver() {
            return driver;
        }

        /**
         * Reference of the item in the registry.
         */
        public DataRef<?> ref() {
            return new DataRef(driver.getType(), name);
        }

        @Override
        public String toString() {
            return String.format("%s [%s]", name, driver.getName());
        }
    }
}
