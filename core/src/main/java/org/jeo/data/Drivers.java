package org.jeo.data;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.jeo.util.Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Driver utility class.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class Drivers {

    /** logger */
    static Logger LOG = LoggerFactory.getLogger(Drivers.class);

    /** list of registered drivers */
    static Set<Class<Driver<?>>> drivers = new LinkedHashSet<Class<Driver<?>>>();

    /**
     * Lists all registered drivers.
     */
    public static Iterator<Driver<?>> list() {
        return list(Driver.class);
    }

    /**
     * Lists all registered drivers that extend from the specified class.
     */
    public static Iterator<Driver<?>> list(final Class<?> filter) {
        final Iterator<Class<Driver<?>>> it = drivers.iterator();
        return new Iterator<Driver<?>>() {
            Driver<?> next;

            @Override
            public boolean hasNext() {
                while(next == null && it.hasNext()) {
                    Class<Driver<?>> clazz = it.next();
                    if (filter == null || filter.isAssignableFrom(clazz)) {
                        try {
                            next = clazz.newInstance();
                            break;
                        } catch (Exception e) {
                            LOG.debug("Unable to instantiate driver class: " + clazz, e);
                        }
                    }
                }

                return next != null;
            }

            @Override
            public Driver<?> next() {
                try {
                    return next;
                }
                finally {
                    next = null;
                }
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * Registers a driver.
     */
    public static void register(Class<Driver<?>> d) {
        drivers.add(d);
    }

    /**
     * Opens a connection to data specified by a file.
     * <p>
     * The optional <tt>class</tt> parameter is used to filter the candidate driver set. For 
     * example to constrain to workspace drivers.
     * <pre><code>
     * Workspace ws = Drivers.open(..., Workspace.class);
     * </code></pre>
     * </p>
     * @param file The file to open.
     * @param clazz Class used to filter registered drivers, may be <code>null</code>.
     * 
     * @return The data object, or <code>null</code> if no suitable driver could be found for the 
     *   specified file.
     *
     * @throws IOException Any connection errors, such as a file system error or 
     *   database connection failure. 
     */
    public static <T> T open(File file, Class<T> clazz) throws IOException {
        Map<Key<?>,Object> opts = new HashMap<Key<?>,Object>();
        opts.put(FileDriver.FILE, file);

        return open(opts, clazz, list(FileDriver.class));
    }

    /**
     * Opens a connection to data described by the specified options.
     * <p>
     * The optional <tt>class</tt> parameter is used to filter the candidate driver set. For 
     * example to constrain to workspace drivers.
     * <pre><code>
     * Workspace ws = Drivers.open(..., Workspace.class);
     * </code></pre>
     * </p>
     * @param opts Connection options.
     * @param clazz Class used to filter registered drivers, may be <code>null</code>.
     * 
     * @return The data object, or <code>null</code> if no suitable driver could be found for the 
     *   specified options.
     *
     * @throws IOException Any connection errors, such as a file system error or 
     *   database connection failure. 
     */
    public static <T> T open(Map<?, Object> opts, Class<T> clazz) throws IOException {
        return open(opts, clazz, list());
    }

    static <T> T open(Map<?, Object> opts, Class<T> clazz, Iterator<Driver<?>> it) 
        throws IOException {
        
        while (it.hasNext()) {
            Driver<?> drv = it.next();
            if (clazz != null && !clazz.isAssignableFrom(drv.getType())) {
                continue;
            }

            if (drv.canOpen(opts)) {
                Object data = drv.open(opts);
                if (data != null) {
                    return (T) data;
                }
            }
        }
        return null;
    }
}
