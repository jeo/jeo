/* Copyright 2013 The jeo project. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jeo.data;

import java.util.Arrays;

/**
 * A map tile, as defined by {@linkplain http://wiki.osgeo.org/wiki/Tile_Map_Service_Specification}.
 */
public class Tile {

    Integer z, x, y;
    byte[] data;
    String mimeType;

    private static final int[] JPG = new int[] { 0xFF, 0xD8, 0xFF };
    private static final int[] PNG = new int[] { 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };
    
    /**
     * Constructs an empty tile object.
     */
    public Tile() {
        this(null, null, null);
    }

    /**
     * Constructs an tile object from an index.
     */
    public Tile(Integer z, Integer x, Integer y) {
        this(z, x, y, null, null);
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
     * Constructs a tile by copying another tile.
     */
    public Tile(Tile t) {
        this(t.z, t.x, t.y, t.data, t.mimeType);
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
        if (mimeType == null && data != null) {
            // try sniffing the data
            if (isMagic(JPG)) {
                mimeType = "image/jpg";
            } else if (isMagic(PNG)) {
                mimeType = "image/png";
            }
        }
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
    
    private boolean isMagic(int[] bytes) {
        boolean match = data.length > bytes.length;
        for (int i = 0; i < bytes.length && match; i++) {
            match &= bytes[i] == data[i];
        }
        return match;
    }

}
