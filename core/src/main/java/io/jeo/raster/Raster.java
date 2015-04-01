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

import com.vividsolutions.jts.geom.Envelope;
import io.jeo.util.Dimension;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import java.util.List;

/**
 * Result of a query against a {@link RasterDataset}.
 * <p>
 *
 * </p>
 */
public class Raster {

    /**
     * The bounds of the raster.
     */
    Envelope bounds;

    /**
     * The projection of the raster.
     */
    CoordinateReferenceSystem crs;

    /**
     * The dimensions of the raster.
     */
    Dimension size;

    /**
     * Bands contained in the raster.
     */
    List<Band> bands;

    /**
     * The raw raster data.
     */
    DataBuffer data;

    /**
     * Nodata for this raster.
     */
    NoData nodata = NoData.NONE;

    /**
     * The bounds of the raster in world coordinates.
     */
    public Envelope bounds() {
        return bounds;
    }

    /**
     * Sets the bounds of the raster in world coordinates.
     */
    public Raster bounds(Envelope bounds) {
        this.bounds = bounds;
        return this;
    }

    /**
     * The world projection of the raster.
     */
    public CoordinateReferenceSystem crs() {
        return crs;
    }

    /**
     * Sets the world projection of the raster.
     */
    public Raster crs(CoordinateReferenceSystem crs) {
        this.crs = crs;
        return this;
    }

    /**
     * The dimensions of the raster.
     */
    public Dimension size() {
        return size;
    }

    /**
     * Sets the dimensions of the raster.
     */
    public Raster size(Dimension size) {
        this.size = size;
        return this;
    }

    /**
     * The raw data for the raster.
     */
    public DataBuffer data() {
        return data;
    }

    /**
     * Sets the raw data for the raster.
     */
    public Raster data(DataBuffer data) {
        this.data = data;
        return this;
    }

    /**
     * The bands contained in the raster.
     */
    public List<Band> bands() {
        return bands;
    }

    /**
     * Sets the bands contained in the raster.
     */
    public Raster bands(List<Band> bands) {
        this.bands = bands;
        return this;
    }

    /**
     * The nodata for the raster.
     */
    public NoData nodata() {
        return nodata;
    }

    /**
     * Sets the nodata for the raster.
     */
    public Raster nodata(NoData nodata) {
        this.nodata = nodata;
        return this;
    }
}
