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
package org.jeo.data;

import com.vividsolutions.jts.geom.Envelope;
import org.jeo.raster.DataType;
import org.jeo.util.Dimension;

/**
 * Describes a query against a {@link RasterDataset} dataset.
 *
 * @author Justin Deoliveira, Boundless
 */
public class RasterQuery {

    /**
     * spatial extent of query.
     */
    Envelope bounds;

    /**
     * band selection
     */
    int[] bands;

    /**
     * Target size for the raster.
     */
    Dimension size;

    /**
     * Data type that band values should be packed into.
     */
    DataType datatype;

    /**
     * Sets the bands to read from the raster dataset.
     * <p>
     * Specifying <tt>null</tt> or an empty array means all bands.
     * </p>
     * @param bands Band indexes (0 based).
     *
     * @return This object.
     */
    public RasterQuery bands(int... bands) {
        this.bands = bands;
        return this;
    }

    /**
     * Sets the bands to read from the raster dataset.
     * <p>
     * Specifying <tt>null</tt> or an empty array means all bands.
     * </p>
     * @param bands Band indexes (0 based).
     *
     * @return This object.
     */
    public RasterQuery bands(Integer[] bands) {
        if (bands == null) {
            this.bands = null;
        }
        else {
            this.bands = new int[bands.length];
            for (int i = 0; i < bands.length; i++) {
                this.bands[i] = bands[i].intValue();
            }
        }
        return this;
    }

    /**
     * The bands to read from the raster dataset.
     *
     * @see #bands(int...)
     */
    public int[] getBands() {
        return bands;
    }

    /**
     * Sets the bounds constraint of the query.
     * <p>
     * Specifying <tt>null</tt> means the bounds of the entire dataset.
     * </p>
     *
     * @return This object.
     */
    public RasterQuery bounds(Envelope bounds) {
        this.bounds = bounds;
        return this;
    }

    /**
     * Bounds constraints on the query, may be <code>null</code> meaning no bounds constraint.
     *
     * @see #bounds(com.vividsolutions.jts.geom.Envelope)
     */
    public Envelope getBounds() {
        return bounds;
    }

    /**
     * Sets the target size of the raster to be read.
     *
     * @param width Raster width.
     * @param height Raster height.
     */
    public RasterQuery size(int width, int height) {
        return size(new Dimension(width, height));
    }

    /**
     * Sets the target size of the raster to be read.
     *
     * @param size Raster dimensions.
     */
    public RasterQuery size(Dimension size) {
        this.size = size;
        return this;
    }

    /**
     * Target size for the raster being read.
     *
     * @see #size(org.jeo.util.Dimension)
     */
    public Dimension getSize() {
        return size;
    }

    /**
     * Sets the type of the returned buffer from a raster query.
     *
     * @param datatype The buffer type.
     *
     * @return This object.
     */
    public RasterQuery datatype(DataType datatype) {
        this.datatype = datatype;
        return this;
    }

    /**
     * The type of the returned buffer from a raster query.
     * <p>
     * Raster datasets are expected to pack data from all bands being
     * query into a single buffer element of this type.
     * </p>
     */
    public DataType getDataType() {
        return datatype;
    }
}
