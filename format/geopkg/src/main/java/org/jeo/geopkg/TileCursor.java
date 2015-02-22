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

import java.io.IOException;

import org.jeo.data.Cursor;
import org.jeo.geopkg.Backend.Results;
import org.jeo.tile.Tile;

public class TileCursor extends Cursor<Tile> {

    final Results rs;
    Boolean next;

    public TileCursor(Results rs) {
        this.rs = rs;
    }

    @Override
    public boolean hasNext() throws IOException {
        if (next == null) {
            try {
                next = rs.next();
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
            t.z(rs.getInt(0));
            t.x(rs.getInt(1));
            t.y(rs.getInt(2));
            t.data(rs.getBytes(3));
        }
        catch(Exception e) {
            throw new IOException(e);
        }

        next = null;
        return t;
    }

    @Override
    public void close() throws IOException {
        rs.close();
    }
}
