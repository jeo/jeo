package org.jeo.geopkg;

import java.io.IOException;
import java.util.List;

import org.jeo.data.Cursor;
import org.jeo.data.Tile;
import org.jeo.data.TileGrid;
import org.jeo.data.TileSet;
import org.jeo.proj.Proj;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

public class GeoPkgTileSet implements TileSet {

    TileEntry entry;
    GeoPkgWorkspace geopkg;

    public GeoPkgTileSet(TileEntry entry, GeoPkgWorkspace geopkg) {
        this.entry = entry;
        this.geopkg = geopkg;
    }

    @Override
    public GeoPackage getDriver() {
        return geopkg.getDriver();
    }

    @Override
    public String getName() {
        return entry.getTableName();
    }

    @Override
    public String getTitle() {
        return entry.getIdentifier();
    }

    @Override
    public String getDescription() {
        return entry.getDescription();
    }

    @Override
    public List<TileGrid> grids() {
        return entry.getTileMatricies();
    }

    @Override
    public TileGrid grid(long z) {
        for (TileGrid g : grids()) {
            if (g.getZoom() == z) {
                return g;
            }
        }
        return null;
    }

    @Override
    public CoordinateReferenceSystem getCRS() {
        int srid = entry.getSrid();
        return srid != -1 ? Proj.crs(srid) : null;
    }

    @Override
    public Envelope bounds() throws IOException {
        return entry.getBounds();
    }

    @Override
    public Tile read(long z, long x, long y) throws IOException {
        Cursor<Tile> c = geopkg.read(entry, (int)z, (int)z, (int)x, (int)x, (int)y, (int)y);
        try {
            if (c.hasNext()) {
                return c.next();
            }
            return null;
        }
        finally {
            c.close();
        }
    }

    @Override
    public Cursor<Tile> read(long z1, long z2, long x1, long x2, long y1, long y2)
            throws IOException {
        return geopkg.read(entry, (int)z1, (int)z2, (int)x1, (int)x2, (int)y1, (int)y2);
    }

    @Override
    public void close() {
    }
}
