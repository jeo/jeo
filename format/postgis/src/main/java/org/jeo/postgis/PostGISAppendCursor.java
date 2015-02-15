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
package org.jeo.postgis;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.jeo.data.Cursor;
import org.jeo.vector.BasicFeature;
import org.jeo.vector.Feature;
import org.jeo.vector.FeatureCursor;

public class PostGISAppendCursor extends FeatureCursor {

    PostGISDataset dataset;
    Connection cx;

    Feature next;

    PostGISAppendCursor(PostGISDataset dataset, Connection cx) {
        super(Cursor.APPEND);
        this.dataset = dataset;
        this.cx = cx;
    }

    @Override
    public boolean hasNext() throws IOException {
        return true;
    }
    
    @Override
    public Feature next() throws IOException {
        return next = new BasicFeature(null, dataset.schema());
    }

    @Override
    protected void doWrite() throws IOException {
        dataset.doInsert(next, cx);
    }

    @Override
    public void close() throws IOException {
        if (cx != null) {
            try {
                cx.close();
            } catch (SQLException e) {
                throw new IOException(e);
            }
        }
        cx = null;
    }
    

}
