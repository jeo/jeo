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
package org.jeo.geopkg;

import static org.jeo.geopkg.GeoPkgWorkspace.LOG;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;

import org.jeo.data.Cursor;
import org.jeo.tile.Tile;

public class TileCursor extends Cursor<Tile> {

    ResultSet results;
    Connection cx;
    Boolean next;

    public TileCursor(ResultSet results, Connection cx) {
        this.results = results;
        this.cx = cx;
    }

    @Override
    public boolean hasNext() throws IOException {
        if (next == null) {
            try {
                next = results.next();
            } catch (Exception e) {
                throw new IOException(e);
            }
        }
        return next;
    }

    @Override
    public Tile next() throws IOException {
        if (next == null || !next) {
            return null;
        }
        Tile t = new Tile();

        try {
            t.setZ(results.getInt(1));
            t.setX(results.getInt(2));
            t.setY(results.getInt(3));
            t.setData(results.getBytes(4)); 
        }
        catch(Exception e) {
            throw new IOException(e);
        }

        next = null;
        return t;
    }

    @Override
    public void close() throws IOException {
        try {
            if (results != null) {
                results.close();
            }
            results = null;
        } catch (Exception e) {
            LOG.debug("error closing result set", e);
        }

        try {
            if (cx != null) {
                cx.close();
            }
        }
        catch(Exception e) {
            LOG.debug("error closing Connection", e);
        }
    }
}
