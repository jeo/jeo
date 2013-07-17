package org.jeo.data;

import java.io.IOException;

/**
 * A layer consisting of {@link Tile} objects.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public interface TileSet extends Dataset {

    /**
     * The pyramid describing the tile structure of the dataset. 
     */
    TilePyramid getPyramid() throws IOException;

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
