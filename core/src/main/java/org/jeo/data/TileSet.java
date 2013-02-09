package org.jeo.data;

import java.io.IOException;
import java.util.List;

public interface TileSet extends Layer {

    List<TileGrid> grids();

    TileGrid grid(long z);

    Tile read(long z, long x, long y) throws IOException;

    Cursor<Tile> read(long z1, long z2, long x1, long x2, long y1, long y2) throws IOException;
}
