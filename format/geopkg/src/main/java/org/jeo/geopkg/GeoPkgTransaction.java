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
import org.jeo.data.Transaction;
import org.jeo.sql.Backend.Session;

/**
 * A wrapper around a {@link Backend.Session}.
 * 
 * @author Ian Schneider <ischneider@boundlessgeo.com>
 */
public class GeoPkgTransaction implements Transaction {
    final Session session;

    GeoPkgTransaction(Session session) throws IOException {
        this.session = session;
        session.beginTransaction();
    }

    @Override
    public void commit() throws IOException {
        session.endTransaction(true);
        session.close();
    }

    @Override
    public void rollback() throws IOException {
        session.endTransaction(false);
        session.close();
    }

}