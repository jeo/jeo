package org.jeo.data;

import java.io.IOException;
import java.util.List;

import com.vividsolutions.jts.geom.Envelope;

public class TileSetView implements Disposable {

    TileDataset tiles;

    public TileSetView(TileDataset tiles) {
        this.tiles = tiles;
    }

    public TileGrid grid(int z) throws IOException {
        return tiles.pyramid().grid(z);
    }

    public TileGrid gridr(int i) throws IOException {
        List<TileGrid> grids = tiles.pyramid().getGrids(); 
        return i < grids.size() ? grids.get(i) : null;
    }

    public Cursor<Tile> cursor(Envelope bbox, int width, int height) throws IOException {
        return tiles.pyramid().cover(bbox, width, height).cursor(tiles);
    }
    
    public Cursor<Tile> cursor(Envelope bbox, double resx, double resy) throws IOException {
        return tiles.pyramid().cover(bbox, resx, resy).cursor(tiles);
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
