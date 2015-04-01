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

import io.jeo.tile.TileDataset;
import io.jeo.vector.VectorDataset;
import io.jeo.util.Key;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

/**
 * A geospatial dataset.
 * <p>
 * Base interface for dataset objects that contains the following properties:
 * <ul>
 *  <li>{@link #bounds()} - The spatial extent of data in the dataset
 *  <li>{@link #crs()} - The projection / coordinate reference system of the data
 * </ul>
 * </p>
 * @author Justin Deoliveira, OpenGeo
 *
 * @see VectorDataset
 * @see TileDataset
 */
public interface Dataset extends Disposable {

    /**
     * The driver used to open the dataset.
     */
    Driver<?> driver();

    /**
     * The driver options for the dataset.
     */
    Map<Key<?>,Object> driverOptions();

    /**
     * Name of the layer.
     * <p>
     * The value used to look up a layer in a workspace.
     * </p>
     */
    String name();

    /**
     * The native coordinate reference system of the layer, or <code>null</code> if unknown. 
     */
    CoordinateReferenceSystem crs() throws IOException;

    /**
     * The extent of the dataset in its native coordinate reference system.
     * <p>
     * In the event the layer is empty this method should return an null envelope with 
     * {@link Envelope#setToNull()}.
     * </p>
     */
    Envelope bounds() throws IOException;
}
