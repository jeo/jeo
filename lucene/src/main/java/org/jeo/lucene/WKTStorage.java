/* Copyright 2015 The jeo project. All rights reserved.
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
package org.jeo.lucene;

import com.spatial4j.core.context.jts.JtsSpatialContext;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;

import java.io.IOException;

public class WKTStorage extends SpatialStorage {

    public WKTStorage(String name, JtsSpatialContext ctx) {
        super(Type.WKT, name, ctx);
    }

    @Override
    public Geometry read(Document doc, int docId, IndexReader reader) throws IOException {
        IndexableField fld = doc.getField(field);
        if (fld == null) {
            return null;
        }
        try {
            return new WKTReader().read(fld.stringValue());
        } catch (ParseException e) {
            throw new IOException(e);
        }
    }
}
