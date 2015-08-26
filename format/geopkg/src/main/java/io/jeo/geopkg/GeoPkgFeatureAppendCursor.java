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

import io.jeo.data.Transaction;
import io.jeo.vector.Feature;
import io.jeo.vector.FeatureAppendCursor;
import io.jeo.vector.ListFeature;
import io.jeo.vector.Schema;
import io.jeo.sql.Backend.Session;

public class GeoPkgFeatureAppendCursor extends FeatureAppendCursor {

    Session session;
    Transaction tx;

    FeatureEntry entry;
    GeoPkgWorkspace ws;
    Schema schema;

    Feature next;

    GeoPkgFeatureAppendCursor(Session session, Transaction tx, FeatureEntry entry, Schema schema, GeoPkgWorkspace ws) throws IOException {
        this.session = session;
        this.tx = tx;
        this.entry = entry;
        this.ws = ws;
        this.schema = schema;

        if (tx == Transaction.NULL) {
            // without a transaction, performance is miserable
            session.beginTransaction();
        }
    }

    @Override
    public Feature next() throws IOException {
        return next = new ListFeature(schema);
    }

    @Override
    public GeoPkgFeatureAppendCursor write() throws IOException {
        ws.insert(entry, next, session);
        return this;
    }

    @Override
    public void close() throws IOException {
        if (session != null) {
            // if not using an "outer" transaction, close the one we created
            if (tx == Transaction.NULL) {
                session.endTransaction(true);
                session.close();
            }
            session = null;
        }
    }
}
