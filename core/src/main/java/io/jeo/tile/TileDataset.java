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
package io.jeo.tile;

import io.jeo.data.Cursor;
import io.jeo.data.Dataset;

import java.io.IOException;

/**
 * A layer consisting of {@link Tile} objects.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public interface TileDataset extends Dataset {

    /**
     * The pyramid describing the tile structure of the dataset. 
     */
    TilePyramid pyramid() throws IOException;

    /**
     * Reads a single tile from the layer by tile index.
     * 
     * @param z The zoom level of the tile.
     * @param x The column of the tile. 
     * @param y The row of the tile.
     * 
     * @return The tile at the specified index, or <code>null</code> if no such tile exists at the 
     * specified index.
     */
    Tile read(long z, long x, long y) throws IOException;

    /**
     * Reads a set of tiles from the layer.
     * <p>
     * Any of the arguments of this method may be <code>-1</code> to specify no constraint.
     * </p>
     * @param z1 The minimum zoom level.
     * @param z2 The maximum zoom level.
     * @param x1 The minimum column index.
     * @param x2 The maximum column index.
     * @param y1 The minimum row index.
     * @param y2 The maximum row index.
     * 
     * @return A cursor over the tile set.
     */
    Cursor<Tile> read(long z1, long z2, long x1, long x2, long y1, long y2) throws IOException;
}
