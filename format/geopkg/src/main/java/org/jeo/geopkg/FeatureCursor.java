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
import java.util.ArrayList;
import java.util.List;

import org.jeo.data.Cursor;
import org.jeo.feature.BasicFeature;
import org.jeo.feature.Feature;
import org.jeo.feature.Field;
import org.jeo.feature.Schema;
import org.jeo.geopkg.geom.GeoPkgGeomReader;
import org.jeo.sql.PrimaryKey;
import org.jeo.sql.PrimaryKeyColumn;

import com.vividsolutions.jts.geom.Geometry;
import org.jeo.geopkg.Backend.Session;
import org.jeo.geopkg.Backend.Results;

public class FeatureCursor extends Cursor<Feature> {

    final FeatureEntry entry;
    final GeoPkgWorkspace workspace;
    final Schema schema;
    final PrimaryKey primaryKey;
    final GeoPkgGeomReader geomReader;
    final Session session;
    final Results results;
    // whether an 'outer' Transaction is in use
    final boolean transaction;

    Boolean next;
    Feature feature;

    FeatureCursor(Session session, Results results, Mode mode, FeatureEntry entry, GeoPkgWorkspace workspace,
            Schema schema, PrimaryKey primaryKey, boolean usingTransaction)
        throws IOException {
        super(mode);

        this.session = session;
        this.results = results;
        this.entry = entry;
        this.workspace = workspace;
        this.schema = schema;
        this.primaryKey = primaryKey;
        this.transaction = usingTransaction;
        // without a transaction, performance is miserable
        if (!transaction && mode != READ) {
            session.beginTransaction();
        }

        this.geomReader = new GeoPkgGeomReader();
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
    public Feature next() throws IOException {
        try {
            if (next != null && next) {
                try {

                    List<Field> fields = schema.getFields();
                    List<Object> values = new ArrayList<Object>();
                    for (int i = 0; i < fields.size(); i++) {
                        Class type = fields.get(i).getType();
                        if (Geometry.class.isAssignableFrom(type)) {
                            byte[] bytes = results.getBytes(i);
                            values.add(bytes != null ? geomReader.read(bytes) : null);
                        }
                        else {
                            values.add(results.getObject(i,type));
                        }
                    }

                    String fid = null;
                    if (primaryKey != null) {
                        StringBuilder buf = new StringBuilder();
                        for (PrimaryKeyColumn pkcol : primaryKey.getColumns()) {
                            String obj = results.getString(pkcol.getName());
                            if (obj != null) {
                                buf.append(obj);
                            }
                            buf.append(".");
                        }

                        buf.setLength(buf.length() - 1);
                        fid = buf.toString();
                    }

                    return feature = new BasicFeature(fid, values, schema);
                } finally {
                    next = null;
                }
            }
            return null;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    protected void doWrite() throws IOException {
        workspace.update(entry, feature, session);
    }

    @Override
    protected void doRemove() throws IOException {
        workspace.delete(entry, feature, session);
    }

    @Override
    public void close() throws IOException {
        results.close();
        if (!transaction) {
            // if not using an 'outer' Transaction, commit and close
            if (mode != Mode.READ) {
                session.endTransaction(true);
            }
            session.close();
        }
    }
}
