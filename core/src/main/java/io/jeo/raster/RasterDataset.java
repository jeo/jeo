/* Copyright 2014 The jeo project. All rights reserved.
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
package io.jeo.raster;

import io.jeo.data.Dataset;
import io.jeo.util.Dimension;

import java.io.IOException;
import java.util.List;

/**
 * A layer consisting of data organized into a grid.
 * <p>
 * A raster dataset is made of up multiple bands, each band representing a
 * component of the overall raster grid.
 * </p>
 *
 * Justin Deoliveira, Boundless
 */
public interface RasterDataset extends Dataset {

    /**
     * The dimensions of the raster.
     */
    Dimension size() throws IOException;

    /**
     * Returns the list of bands describing the contents of the raster.
     */
    List<Band> bands() throws IOException;

    /**
     * Reads data from the raster into a buffer.
     * <p>
     * Implementations must handle data type conversion specified by {@link RasterQuery#bands()}
     * and image resampling specified by {@link RasterQuery#size()}.
     * </p>
     * <p>
     * The returned buffer should always be in read mode.
     * </p>
     * @param query Query describing the band/region/size/etc... to get from the dataset.
     *
     * @return Raster object.
     */
    Raster read(RasterQuery query) throws IOException;
}
