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
package io.jeo.data;

import java.io.IOException;
import java.util.Locale;

import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Reference to a data object.
 * <p>
 * A handle is a lightweight proxy to a data object meant to convey information about the object
 * without necessarily having to load it. The {@link #resolve()} method is used to obtain the
 * underlying data object from the handle.
 * </p>
 *
 * @author Justin Deoliveira, Boundless
 *
 * @param <T>
 */
public abstract class Handle<T> implements Disposable {

    /**
     * object name
     */
    protected String name;

    /**
     * object title
     */
    protected String title;

    /**
     * object description
     */
    protected String description;

    /**
     * object coordinate reference system
     */
    protected CoordinateReferenceSystem crs;

    /**
     * object bounding box
     */
    protected Envelope bounds;

    /**
     * object type
     */
    protected Class<T> type;

    /**
     * object driver
     */
    protected Driver<?> driver;

    /**
     * The live object.
     */
    protected T obj;

    /**
     * Creates a new handle.
     *
     * @param name Name of the object.
     * @param type The type of the data object.
     * @param driver The format driver for the data type.
     */
    @SuppressWarnings("unchecked")
    protected Handle(String name, @SuppressWarnings("rawtypes") Class type, Driver<?> driver) {
        this.name = name;
        this.type = type;
        this.driver = driver;
    }

    protected Handle(String name, Driver<?> driver) {
        this(name, driver.type(), driver);
    }

    /**
     * The name of the data object.
     */
    public String name() {
        return name;
    }

    /**
     * The type of the data object.
     */
    public Class<T> type() {
        return type;
    }

    /**
     * The format driver for the data type.
     */
    public Driver<?> driver() {
        return driver;
    }

    /**
     * Returns the crs of the data object resolving the handle if necessary.
     */
    public CoordinateReferenceSystem crs() throws IOException {
        if (crs == null ) {
            if (Dataset.class.isAssignableFrom(type)) {
                crs = ((Dataset) resolve()).crs();
            }
        }
        return crs;
    }

    /**
     * Returns the bounds of the data object resolving the handle if necessary.
     */
    public Envelope bounds() throws IOException {
        if (bounds == null) {
            if (Dataset.class.isAssignableFrom(type)) {

            }
            bounds = ((Dataset)resolve()).bounds();
        }
        return bounds;
    }

    /**
     * Resolves the handle returning the underlying data object.
     *
     * @return The data object.
     *
     * @throws IOException I/O errors that occur resolving the data object.
     */
    public T resolve() throws IOException {
        if (obj == null) {
            try {
                obj = doResolve();
            }
            catch(ClassCastException e) {
                throw new IOException("handle wrong type, expected: " + type.getName(), e);
            }
        }
        return obj;
    }

    /**
     * Create a Handle that resolves by calling {@link Workspace#get(String)}.
     */
    public static Handle<Dataset> to(String name, final Workspace workspace) {
        return new Handle<Dataset>(name, Dataset.class, workspace.driver()) {
            @Override
            protected Dataset doResolve() throws IOException {
                return workspace.get(name);
            }
        };
    }

    public static Handle<Dataset> to(final Dataset d) {
        return new Handle<Dataset>(d.name(), d.getClass(), d.driver()) {
            @Override
            protected Dataset doResolve() throws IOException {
                return d;
            }
        };
    }

    /**
     * Subclass hook to perform the resolving of the data object.
     *
     * @return The resolved object.
     */
    protected abstract T doResolve() throws IOException;

    @Override
    public void close() {
        if (obj != null && (obj instanceof Disposable)) {
            ((Disposable)obj).close();
        }
        obj = null;
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT,"%s[%s]", type.getSimpleName(), name);
    }
}
