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
package io.jeo.geopkg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.jeo.vector.Feature;
import io.jeo.vector.FeatureCursor;
import io.jeo.vector.Field;
import io.jeo.vector.ListFeature;
import io.jeo.vector.Schema;
import io.jeo.geopkg.geom.GeoPkgGeomReader;
import io.jeo.sql.PrimaryKey;
import io.jeo.sql.PrimaryKeyColumn;

import com.vividsolutions.jts.geom.Geometry;
import io.jeo.sql.Backend.Session;
import io.jeo.sql.Backend.Results;
import io.jeo.vector.SchemaBuilder;

public class GeoPkgFeatureCursor extends FeatureCursor {

    final FeatureEntry entry;
    final GeoPkgWorkspace workspace;
    final Schema schema;
    final PrimaryKey primaryKey;
    final GeoPkgGeomReader geomReader;

    final List<Field> fields;
    // index of primary key columns in result set
    final List<Integer> pkColumns;
    // reusable holder for values as BasicFeature will copy these out
    final List<Object> values;
    // reusable buffer for generating fid
    final StringBuilder buf = new StringBuilder();

    Session session;
    boolean closeSession = true;
    Results results;
    Boolean next;
    Feature feature;

    GeoPkgFeatureCursor(Session session, Results results, FeatureEntry entry, GeoPkgWorkspace workspace,
            Schema schema, PrimaryKey primaryKey, List<String> fields)
        throws IOException {

        this.session = session;
        this.results = results;
        this.entry = entry;
        this.workspace = workspace;
        this.primaryKey = primaryKey;

        geomReader = new GeoPkgGeomReader();

        if (!fields.isEmpty()) {
            // requested fields in schema require rebuilding schema
            this.schema = SchemaBuilder.select(schema, fields);
        } else {
            this.schema = schema;
        }
        this.fields = this.schema.fields();

        values = new ArrayList<Object>(schema.fields().size());

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

    public GeoPkgFeatureCursor closeSession(boolean closeSession) {
        this.closeSession = closeSession;
        return this;
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
                        Class type = fields.get(i).type();
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

                    return feature = new ListFeature(fid, schema, values);
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
    public void close() throws IOException {
        if (results != null) {
            results.close();
            results = null;
        }
        if (session != null) {
            if (closeSession) session.close();
            session = null;
        }
    }
}
