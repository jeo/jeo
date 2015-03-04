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

import org.jeo.vector.BasicFeature;
import org.jeo.vector.Feature;
import org.jeo.vector.FeatureCursor;
import org.jeo.vector.Schema;
import org.jeo.sql.Backend.Session;

public class GeoPkgFeatureAppendCursor extends FeatureCursor {

    final Session session;
    final FeatureEntry entry;
    final GeoPkgWorkspace ws;
    final Schema schema;
    // whether an 'outer' Transaction is in use
    final boolean transaction;

    Feature next;

    GeoPkgFeatureAppendCursor(Session session, FeatureEntry entry, GeoPkgWorkspace ws,
                              Schema schema, boolean usingTransaction) throws IOException {
        super(Mode.APPEND);
        this.session = session;
        this.entry = entry;
        this.ws = ws;
        this.schema = schema;
        this.transaction = usingTransaction;
        // without a transaction, performance is miserable
        if (! usingTransaction) {
            session.beginTransaction();
        }
    }

    @Override
    public boolean hasNext() throws IOException {
        return true;
    }

    @Override
    public Feature next() throws IOException {
        return next = new BasicFeature(null, schema);
    }

    @Override
    protected void doWrite() throws IOException {
        ws.insert(entry, next, session);
    }

    @Override
    public void close() throws IOException {
        // if not using an 'outer' Transaction, commit and close
        if (!transaction) {
            session.endTransaction(true);
            session.close();
        }
    }
}
