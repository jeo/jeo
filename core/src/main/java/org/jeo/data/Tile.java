package org.jeo.data;

import java.util.Arrays;

/**
 * A map tile, as defined by {@linkplain http://wiki.osgeo.org/wiki/Tile_Map_Service_Specification}.
 */
public class Tile {

    Integer z, x, y;
    byte[] data;
    String mimeType;
    
    /**
     * Constructs an empty tile object.
     */
    public Tile() {
    }

    /**
     * Constructs a tile object from its tile index, bounds, and image data.
     */
    public Tile(Integer z, Integer x, Integer y, byte[] data, String mimeType) {
        super();
        this.z = z;
        this.x = x;
        this.y = y;
        this.data = data;
        this.mimeType = mimeType;
    }

    /**
     * The z value (zoom level) of the tile.
     */
    public Integer getZ() {
        return z;
    }

    /**
     * Sets the z value (zoom level) of the tile.
     */
    public void setZ(Integer z) {
        this.z = z;
    }

    /**
     * The x value (column index) of the tile.
     */
    public Integer getX() {
        return x;
    }

    /**
     * Sets the x value (column index) of the tile.
     */
    public void setX(Integer x) {
        this.x = x;
    }

    /**
     * The y value (row index) of the tile.
     */
    public Integer getY() {
        return y;
    }

    /**
     * Sets the y value (row index) of the tile.
     */
    public void setY(Integer y) {
        this.y = y;
    }

    /**
     * The tile image data.
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Sets the tile image data.
     */
    public void setData(byte[] data) {
        this.data = data;
    }

    /**
     * The mime type specifying the format of the tile image data.
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Sets the mime type specifying the format of the tile image data.
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(data);
        result = prime * result + ((mimeType == null) ? 0 : mimeType.hashCode());
        result = prime * result + ((x == null) ? 0 : x.hashCode());
        result = prime * result + ((y == null) ? 0 : y.hashCode());
        result = prime * result + ((z == null) ? 0 : z.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Tile other = (Tile) obj;
        
        if (!Arrays.equals(data, other.data))
            return false;
        if (mimeType == null) {
            if (other.mimeType != null)
                return false;
        } else if (!mimeType.equals(other.mimeType))
            return false;
        if (x == null) {
            if (other.x != null)
                return false;
        } else if (!x.equals(other.x))
            return false;
        if (y == null) {
            if (other.y != null)
                return false;
        } else if (!y.equals(other.y))
            return false;
        if (z == null) {
            if (other.z != null)
                return false;
        } else if (!z.equals(other.z))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return String.format("(z=%d, x=%d, y=%d)", z, y, x);
    }
}
