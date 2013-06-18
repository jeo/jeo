package org.jeo.data;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jeo.feature.Schema;
import org.jeo.util.Key;
import org.jeo.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Driver utility class.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class Drivers {

    /** logger */
    static final Logger LOG = LoggerFactory.getLogger(Drivers.class);

    /** driver registry */
    static final DriverRegistry REGISTRY = new ServiceLoaderDriverRegistry();

    /**
     * Lists all registered drivers.
     */
    public static Iterator<Driver<?>> list() {
        return list(REGISTRY);
    }

    /**
     * Lists all registered drivers from the specified registry.
     */
    public static Iterator<Driver<?>> list(DriverRegistry registry) {
        return list(Driver.class, registry);
    }

    /**
     * Lists all registered drivers that extend from the specified class.
     */
    public static Iterator<Driver<?>> list(final Class<?> filter) {
        return list(filter, REGISTRY);
    }

    /**
     * Lists all registered drivers from the specified registry that extend from the specified class.
     */
    public static Iterator<Driver<?>> list(final Class<?> filter, DriverRegistry registry) {
        final Iterator<? extends Driver<?>> it = registry.list();
        return new Iterator<Driver<?>>() {
            Driver<?> next;

            @Override
            public boolean hasNext() {
                while(next == null && it.hasNext()) {
                    next = it.next();
                    if (filter == null || !filter.isInstance(next)) {
                        next = null;
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
     * Looks up a driver by name.
     *
     * @see Drivers#find(String, DriverRegistry)
     */
    public static Driver<?> find(String name) {
        return find(name, REGISTRY);
    }

    /**
     * Looks up a driver by name from the specified registry.
     * <p>
     * This method does a case-insensitive comparison.
     * </p>
     * 
     * @param name The driver name.
     * 
     * @return The matching driver, or <code>null</code> if no match was found.
     * 
     * @see Driver#getName()
     */
    public static Driver<?> find(String name, DriverRegistry registry) {
        for (Iterator<Driver<?>> it = list(registry); it.hasNext();) {
            Driver<?> d = it.next();
            if (name.equalsIgnoreCase(d.getName()) || d.getAliases().contains(name)) {
                return d;
            }
        }
        return null;
    }

    /**
     * Opens a connection to data specified by a file.
     * 
     * @see Drivers#open(File, Class, DriverRegistry)
     */
    public static <T> T open(File file, Class<T> clazz) throws IOException {
        return open(file, clazz, REGISTRY);
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
    public static <T> T open(File file, Class<T> clazz, DriverRegistry registry) throws IOException {
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
        return open(opts, clazz, REGISTRY);
    }
    
    public static <T> T open(Map<?, Object> opts, Class<T> clazz, DriverRegistry registry) 
        throws IOException {
        return open(opts, clazz, list(registry));
    }

    public static Object open(URI uri) throws IOException {
        return open(uri, REGISTRY);
    }
    
    public static Object open(URI uri, DriverRegistry registry) throws IOException {
        uri = convertFileURI(uri);
        
        Driver<?> d = find(uri, registry);

        Map<String,Object> opts = parseURI(uri, d);

        if (!d.canOpen(opts)) {
            throw new IllegalArgumentException(d.getName() + " driver can't open " + opts);
        }

        Object data = d.open(opts);
        if (data instanceof Workspace && uri.getFragment() != null) {
            data = ((Workspace)data).get(uri.getFragment()); 
        }

        return data;
    }

    public static <T extends VectorData> T create(Schema schema, URI uri, Class<T> clazz) 
        throws IOException {
        return create(schema, uri, clazz, REGISTRY);
    }

    public static <T extends VectorData> T create(Schema schema, URI uri, Class<T> clazz, 
        DriverRegistry registry) throws IOException {
        
        uri = convertFileURI(uri);

        Driver<T> d = find(uri, registry);
        if (!(d instanceof VectorDriver)) {
            throw new IllegalArgumentException(d.getName() + " not a vector driver");
        }

        VectorDriver<T> vd = (VectorDriver<T>) d;
        Map<String,Object> opts = parseURI(uri, d);

        if (!vd.canCreate(opts)) {
            throw new IllegalArgumentException(d.getName() + " driver can't open " + opts);
        }

        return vd.create(opts, schema);
    }

    static <T> Driver<T> find(URI uri, DriverRegistry registry) {
        String scheme = uri.getScheme();
        if (scheme == null) {
            throw new IllegalArgumentException("URI must have a scheme");
        }

        Driver<T> d = (Driver<T>) find(scheme, registry);
        if (d == null) {
            throw new IllegalArgumentException("No matching driver for " + scheme); 
        }

        return d;
    }

    static Map<String,Object> parseURI(URI uri, Driver<?> d) {
        Map<String,Object> opts = new HashMap<String, Object>();
        
        // parse host 
        if (uri.getHost() != null) {
            //use the first key
            if (d.getKeys().isEmpty()) {
                throw new IllegalArgumentException(d.getName() + " declared no keys");
            }

            opts.put(d.getKeys().get(0).getName(), uri.getHost());
        }

        // parse query string
        if (uri.getQuery() != null) {
            String[] kvps = uri.getQuery().split("&");
            for (String kvp : kvps) {
                String[] kv = kvp.split("=");
                if (kv.length != 2) {
                    throw new IllegalArgumentException("Illegal key value pair: " + kvp);
                }

                opts.put(kv[0], kv[1]);
            }
        }

        return opts;
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

    static URI convertFileURI(URI uri) {
        if ("file".equalsIgnoreCase(uri.getScheme())) {
            //hack for files, turn file extension 
            String ext = Util.extension(uri.getPath());
            if (ext != null) {
                try {
                    uri = new URI(String.format("%s://?file=%s%s", ext, uri.getPath(), 
                        uri.getFragment() != null ? "#"+uri.getFragment() : ""));
                } catch (URISyntaxException e) {
                }
            }
        }
        return uri;
    }
}
