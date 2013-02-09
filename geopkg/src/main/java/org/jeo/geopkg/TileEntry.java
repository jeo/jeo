package org.jeo.geopkg;

import java.util.ArrayList;
import java.util.List;

import org.jeo.data.TileGrid;

public class TileEntry extends Entry {

    List<TileGrid> tileMatricies = new ArrayList<TileGrid>();
    Boolean timesTwoZoom;

    public TileEntry() {
        setDataType(DataType.Tile);
    }

    public List<TileGrid> getTileMatricies() {
        return tileMatricies;
    }

    void setTileMatricies(List<TileGrid> tileMatricies) {
        this.tileMatricies = tileMatricies;
    }

    public Boolean isTimesTwoZoom() {
        return timesTwoZoom;
    }

    public void setTimesTwoZoom(Boolean timesTwoZoom) {
        this.timesTwoZoom = timesTwoZoom;
    }

    void init(TileEntry e) {
        super.init(e);
        setTileMatricies(e.getTileMatricies());
        setTimesTwoZoom(e.isTimesTwoZoom());
    }

}
