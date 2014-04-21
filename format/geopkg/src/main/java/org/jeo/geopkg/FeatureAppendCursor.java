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
import java.sql.SQLException;

import org.jeo.data.Cursor;
import org.jeo.feature.BasicFeature;
import org.jeo.feature.Feature;

public class FeatureAppendCursor extends Cursor<Feature> {

    Connection cx;

    FeatureEntry entry;
    GeoPkgWorkspace ws;

    Feature next;

    public FeatureAppendCursor(Connection cx, FeatureEntry entry, GeoPkgWorkspace ws) throws SQLException {
        super(Mode.APPEND);
        this.cx = cx;
        this.entry = entry;
        this.ws = ws;

        cx.setAutoCommit(false);
    }

    @Override
    public boolean hasNext() throws IOException {
        return true;
    }

    @Override
    public Feature next() throws IOException {
        return next = new BasicFeature(null, ws.schema(entry, cx));
    }

    @Override
    protected void doWrite() throws IOException {
        ws.insert(entry, next, cx);
    }

    @Override
    public void close() throws IOException {
        if (cx != null) {
            try {
                cx.commit();
            } catch (SQLException ex) {
                throw new IOException("error committing", ex);
            } finally {
                try {
                    cx.close();
                }
                catch(Exception e) {
                    LOG.debug("error closing Connection", e);
                }
            }
        }
    }
}
