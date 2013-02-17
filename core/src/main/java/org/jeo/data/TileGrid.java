package org.jeo.data;

/**
 * Describes a tile matrix (grid).
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class TileGrid {

    Integer zoom;
    Integer width, height;
    Integer tileWidth, tileHeight;
    Double xRes;
    Double yRes;

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

    /**
     * The zoom level of the tile grid.
     */
    public Integer getZoom() {
        return zoom;
    }

    /**
     * The number of columns of the tile grid.
     */
    public Integer getWidth() {
        return width;
    }

    /**
     * The number of rows of the tile grid.
     */
    public Integer getHeight() {
        return height;
    }

    /**
     * The width of a tile in the matrix, measured in pixels.
     */
    public Integer getTileWidth() {
        return tileWidth;
    }

    /**
     * The height of a tile in the matrix, measured in pixels.
     */
    public Integer getTileHeight() {
        return tileHeight;
    }

    /**
     * The horizontal resolution of the tile grid.
     */
    public Double getXRes() {
        return xRes;
    }

    /**
     * The vertical resolution of the tile grid.
     */
    public Double getYRes() {
        return yRes;
    }
}
