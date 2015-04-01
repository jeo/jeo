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
package io.jeo.mbtiles;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.jeo.data.Cursor;
import io.jeo.data.Driver;
import io.jeo.data.FileData;
import io.jeo.tile.Tile;
import io.jeo.tile.TileDataset;
import io.jeo.tile.TilePyramid;
import io.jeo.tile.TilePyramidBuilder;
import io.jeo.geom.Envelopes;
import io.jeo.proj.Proj;
import io.jeo.util.Key;
import io.jeo.util.Util;
import io.jeo.sql.Backend;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MBTileSet implements TileDataset, FileData {

    protected static Logger LOG = LoggerFactory.getLogger(MBTileSet.class);

    static final String METADATA = "metadata";
    static final String TILES = "tiles";

    static final String PNG = "image/png";
    static final String JPEG = "image/jpeg";

    Backend backend;
    MBTilesOpts opts;

    String tileFormat;

    public MBTileSet(Backend backend, MBTilesOpts opts) {
        this.backend = backend;
        this.opts = opts;
        tileFormat = queryForTileFormat();
    }

    public MBTileSet(File file) {
        this(new JDBCBackend(new MBTilesOpts(file)), new MBTilesOpts(file));
    }

    public String getTileFormat() {
        return tileFormat;
    }

    String queryForTileFormat() {
        try {
            String sql = String.format(Locale.ROOT,"SELECT value FROM %s WHERE name = ?", METADATA);
            Backend.Results results = backend.queryPrepared(sql, "format");
            try {
                if (results.next()) {
                    String format = results.getString(0);
                    return "jpg".equalsIgnoreCase(format) || JPEG.equalsIgnoreCase(format) ? JPEG : PNG;
                }
            } finally {
                results.close();
            }
        }
        catch(IOException e) {
            LOG.error("Error querying for tile format!", e);
        }
        return PNG;
    }

    @Override
    public File file() {
        return opts.file();
    }

    @Override
    public Driver<?> driver() {
        return new MBTiles();
    }

    @Override
    public Map<Key<?>, Object> driverOptions() {
        return (Map) Collections.singletonMap(MBTiles.FILE, file());
    }

    @Override
    public String name() {
        return Util.base(file().getName());
    }

    public String title() {
        try {
            String sql = String.format(Locale.ROOT,"SELECT value FROM %s WHERE name = ?", METADATA);
            Backend.Results results = backend.queryPrepared(sql, "name");
            try {
                if (results.next()) {
                    return results.getString(0);
                }
            } finally {
                results.close();
            }
        }
        catch(IOException e) {
            LOG.error("Error querying for tile name!", e);
        }
        return null;

    }

    public String description() {
        try {
            String sql = String.format(Locale.ROOT,"SELECT value FROM %s WHERE name = ?", METADATA);
            Backend.Results results = backend.queryPrepared(sql, "description");
            try {
                if (results.next()) {
                    return results.getString(0);
                }
            } finally {
                results.close();
            }
        }
        catch(IOException e) {
            LOG.error("Error querying for tile description!", e);
        }
        return null;
    }

    @Override
    public CoordinateReferenceSystem crs() throws IOException {
        return Proj.EPSG_900913;
    }

    @Override
    public Envelope bounds() throws IOException {
        try {
            String sql = String.format(Locale.ROOT,"SELECT value FROM %s WHERE name = ?", METADATA);
            Backend.Results results = backend.queryPrepared(sql, "bounds");
            try {
                if (results.next()) {
                    Envelope b = Envelopes.parse(results.getString(0), true);
                    return b;
                }
            } finally {
                results.close();
            }
        }
        catch(IOException e) {
            LOG.error("Error querying for tile bounds!", e);
        }
        // fall back to bounds of crs
        return Proj.bounds(crs());
    }

    @Override
    public TilePyramid pyramid() throws IOException {
        TilePyramidBuilder tpb = TilePyramid.build().crs(Proj.EPSG_900913);
        tpb.bounds(bounds());

        try {
            String sql = String.format(Locale.ROOT,"SELECT DISTINCT(zoom_level) FROM %s", TILES);
            Backend.Results results = backend.queryPrepared(sql);
            try {
                while(results.next()) {
                    int z = results.getInt(0);
                    int d = (int) Math.pow(2, z);
                    tpb.grid(z, d, d);
                }
            } finally {
                results.close();
            }
        }
        catch(IOException e) {
            LOG.error("Error querying for tile zoom levels!", e);
        }

        return tpb.pyramid();
    }

    @Override
    public Tile read(long z, long x, long y) throws IOException {
        String sql = String.format(Locale.ROOT,"SELECT tile_data FROM %s WHERE zoom_level = %d " +
                "AND tile_column = %d AND tile_row = %d", TILES, z, x, y);
        try {
            Backend.Results results = backend.queryPrepared(sql);
            try {
                if (results.next()) {
                    return new Tile((int)z, (int)x, (int)y, results.getBytes(0), tileFormat);
                }
            } finally {
                results.close();
            }
        }
        catch(IOException e) {
            LOG.error(String.format(Locale.ROOT,"Error reading tile %s/%s/%s!", z, x, y), e);
        }

        return null;
    }

    @Override
    public Cursor<Tile> read(long z1, long z2, long x1, long x2, long y1, long y2) throws IOException {
        final List<String> q = new ArrayList<String>();

        if (z1 > -1) {
            q.add("zoom_level >= " + z1);
        }
        if (z2 > -1) {
            q.add("zoom_level <= " + z2);
        }
        if (x1 > -1) {
            q.add("tile_column >= " + x1);
        }
        if (x2 > -1) {
            q.add("tile_column <= " + x2);
        }
        if (y1 > -1) {
            q.add("tile_row >= " + y1);
        }
        if (y2 > -1) {
            q.add("tile_row <= " + y2);
        }

        StringBuilder where = new StringBuilder();
        if (!q.isEmpty()) {
            for (String s : q) {
                where.append(s).append(" AND ");
            }
            where.setLength(where.length()-5);
        }

        String sql = String.format(Locale.ROOT,"SELECT zoom_level, tile_column, tile_row, tile_data FROM %s WHERE %s ORDER BY zoom_level", TILES, where);
        Backend.Results results = backend.query(sql);
        return new TileCursor(results);
    }

    @Override
    public void close() {
        try {
            backend.close();
        } catch(IOException e) {
            // LOG
        }
    }

}