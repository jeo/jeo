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
import org.jeo.vector.BasicFeature;
import org.jeo.vector.Feature;
import org.jeo.vector.Field;
import org.jeo.vector.Schema;
import org.jeo.geopkg.geom.GeoPkgGeomReader;
import org.jeo.sql.PrimaryKey;
import org.jeo.sql.PrimaryKeyColumn;

import com.vividsolutions.jts.geom.Geometry;
import org.jeo.geopkg.Backend.Session;
import org.jeo.geopkg.Backend.Results;
import org.jeo.vector.SchemaBuilder;

public class FeatureCursor extends Cursor<Feature> {

    final FeatureEntry entry;
    final GeoPkgWorkspace workspace;
    final Schema schema;
    final PrimaryKey primaryKey;
    final GeoPkgGeomReader geomReader;
    final Session session;
    final Results results;
    final List<Field> fields;
    // whether an 'outer' Transaction is in use
    final boolean transaction;
    // index of primary key columns in result set
    final List<Integer> pkColumns;
    // reusable holder for values as BasicFeature will copy these out
    final List<Object> values;
    // reusable buffer for generating fid
    final StringBuilder buf = new StringBuilder();

    Boolean next;
    Feature feature;

    FeatureCursor(Session session, Results results, Mode mode, FeatureEntry entry, GeoPkgWorkspace workspace,
            Schema schema, PrimaryKey primaryKey, boolean usingTransaction, List<String> fields)
        throws IOException {
        super(mode);

        this.session = session;
        this.results = results;
        this.entry = entry;
        this.workspace = workspace;
        this.primaryKey = primaryKey;
        this.transaction = usingTransaction;
        geomReader = new GeoPkgGeomReader();

        // without a transaction, performance is miserable
        if (!transaction && mode != READ) {
            session.beginTransaction();
        }

        if (!fields.isEmpty()) {
            // requested fields in schema require rebuilding schema
            this.schema = SchemaBuilder.selectFields(schema, fields);
        } else {
            this.schema = schema;
        }
        this.fields = this.schema.getFields();

        values = new ArrayList<Object>(schema.getFields().size());

        pkColumns = new ArrayList<Integer>(primaryKey.getColumns().size());
        int end = fields.size();
        for (PrimaryKeyColumn pkcol : primaryKey.getColumns()) {
            // if schema has reduced fields, they will be present in the end
            int idx = this.schema.indexOf(pkcol.getName());
            if (idx >= 0) {
                pkColumns.add(idx);
            } else {
                pkColumns.add(end++);
            }
        }
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
                    values.clear();

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

                    if (!pkColumns.isEmpty()) {
                        buf.delete(0, buf.length());
                        for (int i = 0; i < pkColumns.size(); i++) {
                            Object obj = results.getString(pkColumns.get(i));
                            if (obj != null) {
                                buf.append(obj);
                            }
                            buf.append(".");
                        }

                        if (buf.length() > 0) {
                            buf.setLength(buf.length() - 1);
                        }
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
