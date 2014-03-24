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
package org.jeo.tile;

/**
 * Describes a tile matrix (grid).
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class TileGrid {

    Integer z;
    Integer width, height;
    Double xRes, yRes;

    /**
     * Constructs a tile grid object.
     * 
     * @param z Zoom level of the grid.
     * @param width Number of tiles horizontally in the grid.
     * @param height Number of tiles vertically in the grid.
     * @param xRes The x resolution of tiles.
     * @param yRes The y resolution of tiles.
     */
    public TileGrid(Integer z, Integer width, Integer height, Double xRes, Double yRes) {
        super();
        this.z = z;
        this.width = width;
        this.height = height;
        this.xRes = xRes;
        this.yRes = yRes;
    }

    /**
     * The z (zoom) level of the tile grid.
     */
    public Integer getZ() {
        return z;
    }

    /**
     * The number of columns of the tile grid.
     */
    public Integer getWidth() {
        return width;
    }

    /**
     * The number of rows of the tile grid.
     */
    public Integer getHeight() {
        return height;
    }

    /**
     * The horizontal resolution of the tile grid.
     * <p>
     * More specifically the horizontal size of a single pixel in "map" units. 
     * </p>
     */
    public Double getXRes() {
        return xRes;
    }

    /**
     * The vertical resolution of the tile grid.
     * <p>
     * More specifically the vertical size of a single pixel in "map" units. 
     * </p>
     */
    public Double getYRes() {
        return yRes;
    }
}
