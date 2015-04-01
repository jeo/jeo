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

import java.util.List;

import io.jeo.proj.Proj;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Builder for {@link TilePyramid} objects.
 * <p>
 * Example usage:
 * <pre>
 * TilePyramid schema = TilePyramid.build().bounds(-180,-90,180,90).crs("EPSG:4326")
 *   .tileSize(256, 256).grid(2,1).grid().grid(2, 8, 4).pyramid();
 * </pre>
 * </p>
 * @author Justin Deoliveira, OpenGeo
 *
 */
public class TilePyramidBuilder {

    TilePyramid tp;

    /**
     * Creates a new builder.
     */
    public TilePyramidBuilder() {
        tp = new TilePyramid();
    }

    /**
     * Sets bounds of the pyramid.
     */
    public TilePyramidBuilder bounds(double minx, double miny, double maxx, double maxy) {
        return bounds(new Envelope(minx, maxx, miny, maxy));
    }

    /**
     * Sets bounds of the pyramid.
     */
    public TilePyramidBuilder bounds(Envelope bounds) {
        tp.bounds(bounds);
        return this;
    }

    /**
     * Sets crs of the pyramid.
     */
    public TilePyramidBuilder crs(String srs) {
        return crs(Proj.crs(srs));
    }

    /**
     * Sets crs of the pyramid.
     */
    public TilePyramidBuilder crs(CoordinateReferenceSystem crs) {
        tp.crs(crs);
        return this;
    }

    /**
     * Sets the tile dimensions for the pyramid.
     */
    public TilePyramidBuilder tileSize(int width, int height) {
        tp.tileWidth(width);
        tp.tileHeight(height);
        return this;
    }

    /**
     * Sets the origin of the tile coordinate system.
     */
    public TilePyramidBuilder origin(TilePyramid.Origin origin) {
        tp.origin(origin);
        return this;
    }

    /**
     * Creates a new level in the pyramid.
     *  
     * @param z The zoom level of the grid level.
     * @param width The number of horizontal tiles in the level.
     * @param height The number of vertical tiles in the level. 
     * 
     */
    public TilePyramidBuilder grid(int z, int width, int height) {
        double xres = tp.bounds().getWidth() / ((double)width) / ((double)tp.tileWidth());
        double yres = tp.bounds().getHeight() / ((double)height) / ((double)tp.tileHeight());

        tp.grids().add(new TileGrid(z, width, height, xres, yres));
        return this;
    }

    /**
     * Creates a new level in the pyramid automatically determining the zoom level.
     * <p>
     * If no grids have been previously added the z level is set to 0. Otherwise the z level is 
     * the previously added z level + 1.
     * </p> 
     * @param width The number of horizontal tiles in the level.
     * @param height The number of vertical tiles in the level.
     */
    public TilePyramidBuilder grid(int width, int height) {
        int z;

        List<TileGrid> grids = tp.grids();
        if (grids.isEmpty()) {
            z = 0;
        }
        else {
            TileGrid last = grids.get(grids.size()-1);
            z = last.z()+1;
        }

        return grid(z, width, height);
    }

    /**
     * Creates a new level in the pyramid automatically determining the zoom level and grid 
     * dimensions.
     * <p>
     * If not grids have been previously added to the pyramid the z level is set to 0 and grid 
     * dimensions inferred from the pyramid bounds. Otherwise the grid z level is the previous
     * z value + 1, and the grid dimensions are the previous dimensions * 2.
     * </p>
     */
    public TilePyramidBuilder grid() {
        List<TileGrid> grids = tp.grids();
        if (grids.isEmpty()) {
            Envelope bounds = tp.bounds();
            if (bounds != null) {
                if (bounds.getWidth() > bounds.getHeight()) {
                    return grid(0, (int)(bounds.getWidth()/bounds.getHeight()), 1);
                }
                else {
                    return grid(0, 1, (int)(bounds.getHeight()/bounds.getWidth()));
                }
            }
            else {
                grid(0, 2, 1);
            }
        }
        else {
            TileGrid last = grids.get(grids.size()-1);
            return grid(last.z()+1, last.width()*2, last.height()*2);
        }
        return this;
    }

    /**
     * Adds a number of levels to the pyramid.
     * <p>
     * This method is equivalent to calling {@link #grid()} <tt>n</tt> times.
     * </p>
     * @param n The number of levels to add to the pyramid.     * @return
     */
    public TilePyramidBuilder grids(int n) {
        for (int i = 0; i < n; i++) grid();
        return this;
    }

    /**
     * Returns the built pyramid.
     */
    public TilePyramid pyramid() {
        return tp;
    }
}
