package org.jeo.data;

public class TileGrid {

    Integer zoom;
    Integer width, height;
    Integer tileWidth, tileHeight;
    Double xRes;
    Double yRes;

    public TileGrid() {
    }

    public TileGrid(Integer zoom, Integer width, Integer height, Integer tileWidth, 
        Integer tileHeight, Double xRes, Double yRes) {
        super();
        this.zoom = zoom;
        this.width = width;
        this.height = height;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.xRes = xRes;
        this.yRes = yRes;
    }

    public Integer getZoom() {
        return zoom;
    }

    public void setZoom(Integer zoom) {
        this.zoom = zoom;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Integer getTileWidth() {
        return tileWidth;
    }

    public void setTileWidth(Integer tileWidth) {
        this.tileWidth = tileWidth;
    }

    public Integer getTileHeight() {
        return tileHeight;
    }

    public void setTileHeight(Integer tileHeight) {
        this.tileHeight = tileHeight;
    }

    public Double getXRes() {
        return xRes;
    }

    public void setXRes(Double xRes) {
        this.xRes = xRes;
    }

    public Double getYRes() {
        return yRes;
    }

    public void setYRes(Double yRes) {
        this.yRes = yRes;
    }
}
