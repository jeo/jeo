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
import java.util.Map;

import io.jeo.vector.Schema;
import io.jeo.util.Key;
import io.jeo.vector.VectorDataset;

/**
 * A container of {@link Dataset} objects.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public interface Workspace extends Disposable {

    /**
     * The driver used to open the workspace.
     */
    Driver<?> driver();

    /**
     * The driver options for the workspace.
     */
    Map<Key<?>,Object> driverOptions();

    /**
     * The names of all datasets of the workspace.
     * 
     * @return Iterable over datasets. 
     */
    Iterable<Handle<Dataset>> list() throws IOException;

    /**
     * Returns a dataset object by name.
     * 
     * @param name Name of the dataset.
     * 
     * @return The Dataset object, or <code>null</code> if no such dataset exists.
     */
    Dataset get(String name) throws IOException;

    /**
     * Creates a new vector dataset in the workspace.
     * <p>
     * This method should throw {@link UnsupportedOperationException} if the workspace is not 
     * capable of creating new vector datasets. In this case the driver should also report that
     * {@link io.jeo.vector.VectorDriver.Capability#CREATE} is not supported.
     * </p>
     * @param schema The schema of the vector dataset.
     * 
     */
    VectorDataset create(Schema schema) throws IOException;

    /**
     * Destroys the dataset within the workspace.
     * <p>
     * This method should throw {@link UnsupportedOperationException} if the workspace is not
     * capable of destroying datasets. In this case the driver should also report that
     * {@link io.jeo.vector.VectorDriver.Capability#DESTROY} is not supported.
     * </p>
     *
     * @param name The name of the dataset.
     */
    void destroy(String name) throws IOException;

    /**
     * Closes the workspace.
     * <p>
     * Application code should always call this method when the workspace is no longer needed. 
     * </p>
     */
    void close();
}
