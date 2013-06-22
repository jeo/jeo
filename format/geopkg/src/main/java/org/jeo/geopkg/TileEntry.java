package org.jeo.geopkg;

import java.util.ArrayList;
import java.util.List;

import org.jeo.data.TileGrid;
import org.jeo.data.TilePyramid;

public class TileEntry extends Entry {

    TilePyramid tilePyramid;
    Boolean timesTwoZoom;

    public TileEntry() {
        setDataType(DataType.Tile);
    }

    public TilePyramid getTilePyramid() {
        return tilePyramid;
    }

    public void setTilePyramid(TilePyramid tilePyramid) {
        this.tilePyramid = tilePyramid;
    }

    public Boolean isTimesTwoZoom() {
        return timesTwoZoom;
    }

    public void setTimesTwoZoom(Boolean timesTwoZoom) {
        this.timesTwoZoom = timesTwoZoom;
    }

    void init(TileEntry e) {
        super.init(e);
        setTilePyramid(e.getTilePyramid());
        setTimesTwoZoom(e.isTimesTwoZoom());
    }

}
