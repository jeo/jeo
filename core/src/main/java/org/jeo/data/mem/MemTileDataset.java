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
package org.jeo.data.mem;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.jeo.data.Cursor;
import org.jeo.data.Driver;
import org.jeo.tile.Tile;
import org.jeo.data.TileDataset;
import org.jeo.tile.TileGrid;
import org.jeo.tile.TilePyramid;
import org.jeo.util.Key;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

public class MemTileDataset implements TileDataset {

    String name;
    TilePyramid pyramid;
    Tile[][][] tiles; // z, y, x

    public MemTileDataset(TilePyramid pyramid) {
        this.pyramid = pyramid;
        tiles = new Tile[pyramid.getGrids().size()][][];
    }

    @Override
    public Driver<?> getDriver() {
        return new Memory();
    }
    
    @Override
    public Map<Key<?>, Object> getDriverOptions() {
        return Collections.emptyMap();
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public String getTitle() {
        return null;
    }
    
    @Override
    public String getDescription() {
        return null;
    }
    
    @Override
    public CoordinateReferenceSystem crs() throws IOException {
        return pyramid.getCRS();
    }

    @Override
    public Envelope bounds() throws IOException {
        return pyramid.getBounds();
    }

    @Override
    public TilePyramid pyramid() throws IOException {
        return pyramid;
    }

    @Override
    public Tile read(long z, long x, long y) throws IOException {
        TileGrid grid = pyramid.grid((int) z);
        if (grid == null) {
            //throw new IOException(String.format("no grid for zoom level %d", z));
            return null;
        }

        if (y >= grid.getHeight()) {
            return null;
            //throw new IOException(String.format("y must be < %d", grid.getHeight()));
        }
        
        if (x >= grid.getWidth()) {
            return null;
            //throw new IOException(String.format("x must be < %d", grid.getWidth()));
        }

        return tiles[(int)z][(int)y][(int)x];
    }

    @Override
    public Cursor<Tile> read(final long z1, final long z2, final long x1, final long x2, 
            final long y1, final long y2) throws IOException {
        return new Cursor<Tile>() {
            int z = (int) z1;
            int y = (int) y1;
            int x = (int) x1;
            Tile next;

            @Override
            public boolean hasNext() throws IOException {
                x++;
                if (x > x2) {
                    x = (int) x1;
                    y++;
                    if (y > y2) {
                        y = (int) y1;
                        z++;
                    }
                }

                return z < z2;
            }

            @Override
            public Tile next() throws IOException {
                return tiles[z][y][x];
            }

            @Override
            public void close() throws IOException {
            }
        };
    }

    public void put(int z, Tile[][] tiles) {
        if (z > this.tiles.length) {
            throw new IndexOutOfBoundsException(String.format(
                "zoom level %d greater than pyramid depth %d", z, this.tiles.length)); 
        }

        TileGrid grid = pyramid.grid(z);
        if (tiles.length != grid.getHeight()) {
            throw new IllegalArgumentException(String.format(
                "number of vertical tiles  %d != grid height %d", tiles.length, grid.getHeight()));
        }

        for (Tile[] row : tiles) {
            if (row.length != grid.getWidth()) {
                throw new IndexOutOfBoundsException(String.format(
                    "number of horizontal tiles  %d != grid width %d", tiles.length, grid.getWidth()));
            }
        }


        this.tiles[z] = tiles;
    }

    @Override
    public void close() {
    }
}
