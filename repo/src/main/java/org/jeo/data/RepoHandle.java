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

import java.io.IOException;

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
public abstract class RepoHandle<T> extends Handle {

    /**
     * Creates a new handle.
     * 
     * @param name Name of the object.
     * @param type The type of the data object.
     * @param driver The format driver for the data type.
     */
    @SuppressWarnings("unchecked")
    protected RepoHandle(String name, @SuppressWarnings("rawtypes") Class type, Driver<?> driver) {
        super(name, type, driver);
    }

    protected RepoHandle(String name, Driver<?> driver) {
        this(name, driver.type(), driver);
    }

    /**
     * Create a Handle that resolves by calling {@link DataRepository#get(String, Class)}.
     */
    public static <T> RepoHandle<T> to(String name, Driver<?> driver, final DataRepository repo) {
        return (RepoHandle<T>) to(name, driver.type(), driver, repo);
    }

    /**
     * Create a Handle that resolves by calling {@link DataRepository#get(String, Class)} 
     * specifying explicitly the type of object.
     */
    public static <T> RepoHandle<T> to(String name, Class<T> type, Driver<?> driver,
        final DataRepository repo) {
        return new RepoHandle<T>(name, type, driver) {
            @Override
            protected T doResolve() throws IOException {
                return (T) repo.get(name, type);
            }
        };
    }

    /**
     * Create a Handle that resolves by calling {@link Workspace#get(String)}.
     */
    public static RepoHandle<Dataset> to(String name, final Workspace workspace) {
        return new RepoHandle<Dataset>(name, Dataset.class, workspace.driver()) {
            @Override
            protected Dataset doResolve() throws IOException {
                return workspace.get(name);
            }
        };
    }

    public static RepoHandle<Dataset> to(final Dataset d) {
        return new RepoHandle<Dataset>(d.name(), d.getClass(), d.driver()) {
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
        return String.format("%s[%s]", type.getSimpleName(), name);
    }
}
