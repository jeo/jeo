/* Copyright 2013 The jeo project. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jeo.data;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jeo.vector.Schema;
import org.jeo.util.Util;
import org.jeo.vector.VectorDataset;
import org.jeo.vector.VectorDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Driver utility class.
 * <p>
 * This class is used to abstract away driver specifics when interacting with data. For example it
 * can be used to create a data object from a file.
 * <pre><code>
 *   Driver.open(new File("states.json"));
 * </code></pre>
 * </p>
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class Drivers {

    /** logger */
    static final Logger LOG = LoggerFactory.getLogger(Drivers.class);

    /** driver registry */
    public static final DriverRegistry REGISTRY = new ServiceLoaderDriverRegistry();

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
     * @see Driver#name()
     */
    public static Driver<?> find(String name, DriverRegistry registry) {
        for (Iterator<Driver<?>> it = list(registry); it.hasNext();) {
            Driver<?> d = it.next();
            if (name.equalsIgnoreCase(d.name()) || d.aliases().contains(name)) {
                return d;
            }
        }
        return null;
    }

    /**
     * Looks up a driver from a data uri.
     * 
     * @see Drivers#find(URI, DriverRegistry)}.
     */
    public static Driver<?> find(URI uri) {
        return find(uri, REGISTRY);
    }

    /**
     * Looks up a driver from a data uri.
     * 
     * @param uri A uri defining a data source, as described by 
     * {@link #open(URI, Class, DriverRegistry)}.
     * 
     * @return The matching driver, or <code>null</code> if no match was found.
     */
    public static <T> Driver<T> find(URI uri, DriverRegistry registry) {
        uri = convertFileURI(uri);

        String scheme = uri.getScheme();
        if (scheme == null) {
            LOG.debug("URI must have scheme: " + uri);
            return null;
            //throw new IllegalArgumentException("URI must have a scheme");
        }

        Driver<T> d = (Driver<T>) find(scheme, registry);
        if (d == null) {
            LOG.debug("No matching driver for " + scheme);
            return null;
        }

        return d;
    }
    
    /**
     * Opens a connection to data specified by a file.
     * 
     * @see Drivers#open(File, Class, DriverRegistry)
     */
    public static Object open(File file) throws IOException {
        return open(file, REGISTRY);
    }

    /**
     * Opens a connection to data specified by a file using the specified driver registry.
     * 
     * @see Drivers#open(File, Class, DriverRegistry)
     */
    public static Object open(File file, DriverRegistry registry) throws IOException {
        return open(file, Object.class, registry);
    }

    
    /**
     * Opens a connection to data specified by a file expecting an object of the specified class.
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
        return open(file.toURI(), clazz, registry);
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

    /**
     * Opens a connection to data described by the specified uri.
     * 
     * @see Drivers#open(URI, Class, DriverRegistry)
     */
    public static Object open(URI uri) throws IOException {
        return open(uri, REGISTRY);
    }

    /**
     * Opens a connection to data described by the specified uri with the specified driver 
     * registry.
     * 
     * @see Drivers#open(URI, Class, DriverRegistry)
     */
    public static Object open(URI uri, DriverRegistry registry) throws IOException {
        return open(uri, Object.class, registry);
    }

    /**
     * Opens a connection to data of the specified type described by the specified uri.
     *
     * @see Drivers#open(URI, Class, DriverRegistry)
     */
    public static <T> T open(URI uri, Class<T> clazz) throws IOException {
        return open(uri, clazz, REGISTRY);
    }

    /**
     * Opens a connection to data described by the specified uri.
     * <p>
     * The <tt>uri</tt> can take one of two forms. The first is a file uri:
     * <pre>
     *   file:&lt;path>
     * </pre>
     * For example <tt>file:/Users/jdeolive/foo.json</tt> specifies a connection to a GeoJSON file.
     * </p>
     * <p>
     * The second form uses the uri components to describe different aspect of the connection:
     * <pre>
     * [&lt;driver>://][&lt;primary-option>][?&lt;secondary-options>]*][#&lt;dataset>]
     * </pre>
     * Where:
     * <ul>
     *  <li><tt>driver</tt> is the driver name or alias
     *  <li><tt>primary-option</tt> is the "main" option for that driver, for example in the case
     *  of file based drivers this would be the file path, in the case of database based drivers
     *  this would be the database name.
     *  <li><tt>secondary-options</tt> are additional driver options
     *  <li><tt>dataset</tt> is the name of a dataset within a workspace
     * </ul>
     * For example <tt>pg://jeo?host=locahost&port=5432&user=bob#states</tt> specifies a 
     * connection to a PostGIS table named "states", in a database named "jeo", as user "bob", to 
     * a server running on localhost, port 5432.  
     * </p>
     * <p>
     * The optional <tt>class</tt> parameter is used to filter the candidate driver set. For 
     * example to constrain to workspace drivers.
     * <pre><code>
     * Workspace ws = Drivers.open(..., Workspace.class);
     * </code></pre>
     * </p>
     * @param uri uri specifying connection options.
     * @param clazz Class used to filter registered drivers, may be <code>null</code>.
     * @param registry the non-null registry to search for drivers
     * 
     * @return The data object, or <code>null</code> if no suitable driver could be found for the 
     *   specified options.
     *
     * @throws IOException Any connection errors, such as a file system error or 
     *   database connection failure. 
     */
    public static <T> T open(URI uri, Class<T> clazz, DriverRegistry registry) throws IOException {
        uri = convertFileURI(uri);
        
        Driver<?> d = find(uri, registry);
        if (d == null) {
            return null;
        }

        Map<String,Object> opts = parseURI(uri, d);

        Object data = d.open(opts);
        if (data instanceof Workspace && uri.getFragment() != null) {
            data = ((Workspace)data).get(uri.getFragment()); 
        }
        if (clazz == Workspace.class && data instanceof Dataset) {
            data = new SingleWorkspace((Dataset) data);
        }

        return clazz.cast(data);
    }

    public static <T extends VectorDataset> T create(Schema schema, URI uri, Class<T> clazz)
        throws IOException {
        return create(schema, uri, clazz, REGISTRY);
    }

    public static <T extends VectorDataset> T create(Schema schema, URI uri, Class<T> clazz, 
        DriverRegistry registry) throws IOException {
        
        uri = convertFileURI(uri);

        Driver<T> d = find(uri, registry);
        if (d == null) {
            throw new IllegalArgumentException("No driver for " + uri);
        }

        if (!(d instanceof VectorDriver)) {
            throw new IllegalArgumentException(d.name() + " not a vector driver");
        }

        VectorDriver<?> vd = (VectorDriver<?>) d;
        Map<String,Object> opts = parseURI(uri, d);

        if (!vd.canCreate(opts, null)) {
            throw new IllegalArgumentException(d.name() + " driver can't open " + opts);
        }

        Object obj =  vd.create(opts, schema);
        if (obj instanceof VectorDataset) {
            return (T) obj;
        }
        else if (obj instanceof Workspace) {
            Workspace ws = (Workspace) obj;
            return (T) ws.get(schema.getName());
        }

        throw new IllegalArgumentException(d.name() + " driver returned " + obj);
    }

    static Map<String,Object> parseURI(URI uri, Driver<?> d) {
        Map<String,Object> opts = new HashMap<String, Object>();
        
        // parse host / path
        String first = uri.getHost() != null ? uri.getHost() : uri.getPath();
        if (first != null) {
            //use the first key
            if (!d.keys().isEmpty()) {
                opts.put(d.keys().get(0).getName(), first);
            }
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
            if (clazz != null && !clazz.isAssignableFrom(drv.type())) {
                continue;
            }

            if (drv.canOpen(opts, null)) {
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
