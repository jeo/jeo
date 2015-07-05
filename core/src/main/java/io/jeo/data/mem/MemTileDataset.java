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
package io.jeo.data.mem;

import java.io.IOException;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import io.jeo.data.Cursor;
import io.jeo.data.Driver;
import io.jeo.tile.Tile;
import io.jeo.tile.TileDataset;
import io.jeo.tile.TileGrid;
import io.jeo.tile.TilePyramid;
import io.jeo.util.Key;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

public class MemTileDataset implements TileDataset {

    String name;
    TilePyramid pyramid;
    Tile[][][] tiles; // z, y, x

    public MemTileDataset(String name, TilePyramid pyramid) {
        this.name = name;
        this.pyramid = pyramid;
        tiles = new Tile[pyramid.grids().size()][][];
    }

    @Override
    public Driver<?> driver() {
        return new Memory();
    }
    
    @Override
    public Map<Key<?>, Object> driverOptions() {
        return Collections.emptyMap();
    }
    
    @Override
    public String name() {
        return name;
    }
    
    @Override
    public CoordinateReferenceSystem crs() throws IOException {
        return pyramid.crs();
    }

    @Override
    public Envelope bounds() throws IOException {
        return pyramid.bounds();
    }

    @Override
    public TilePyramid pyramid() throws IOException {
        return pyramid;
    }

    @Override
    public Tile read(long z, long x, long y) throws IOException {
        TileGrid grid = pyramid.grid((int) z);
        if (grid == null) {
            //throw new IOException(String.format(Locale.ROOT,"no grid for zoom level %d", z));
            return null;
        }

        if (y >= grid.height()) {
            return null;
            //throw new IOException(String.format(Locale.ROOT,"y must be < %d", grid.getHeight()));
        }
        
        if (x >= grid.width()) {
            return null;
            //throw new IOException(String.format(Locale.ROOT,"x must be < %d", grid.getWidth()));
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
            public void close() {
            }
        };
    }

    public void put(int z, Tile[][] tiles) {
        if (z > this.tiles.length) {
            throw new IndexOutOfBoundsException(String.format(Locale.ROOT,
                "zoom level %d greater than pyramid depth %d", z, this.tiles.length)); 
        }

        TileGrid grid = pyramid.grid(z);
        if (tiles.length != grid.height()) {
            throw new IllegalArgumentException(String.format(Locale.ROOT,
                "number of vertical tiles  %d != grid height %d", tiles.length, grid.height()));
        }

        for (Tile[] row : tiles) {
            if (row.length != grid.width()) {
                throw new IndexOutOfBoundsException(String.format(Locale.ROOT,
                    "number of horizontal tiles  %d != grid width %d", tiles.length, grid.width()));
            }
        }


        this.tiles[z] = tiles;
    }

    @Override
    public void close() {
    }
}
