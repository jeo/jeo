package org.jeo.data;

import java.io.IOException;
import java.util.List;

import com.vividsolutions.jts.geom.Envelope;

public class TileSetView implements Disposable {

    TileSet tiles;

    public TileSetView(TileSet tiles) {
        this.tiles = tiles;
    }

    public TileGrid grid(int z) throws IOException {
        return tiles.getPyramid().grid(z);
    }

    public TileGrid gridr(int i) throws IOException {
        List<TileGrid> grids = tiles.getPyramid().getGrids(); 
        return i < grids.size() ? grids.get(i) : null;
    }

    public Cursor<Tile> cursor(Envelope bbox, int width, int height) throws IOException {
        return tiles.getPyramid().cover(bbox, width, height).cursor(tiles);
    }
    
    public Cursor<Tile> cursor(Envelope bbox, double resx, double resy) throws IOException {
        return tiles.getPyramid().cover(bbox, resx, resy).cursor(tiles);
    }

    @Override
    public void close() {
        if (tiles != null) {
            tiles.close();
            tiles = null;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        close();
    }
}
