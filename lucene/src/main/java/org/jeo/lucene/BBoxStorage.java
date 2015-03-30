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
import com.spatial4j.core.io.LegacyShapeReadWriterFormat;
import com.spatial4j.core.shape.Rectangle;
import com.spatial4j.core.shape.Shape;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexReader;

import java.io.IOException;

public class BBoxStorage extends SpatialStorage {

    public BBoxStorage(String field, JtsSpatialContext ctx) {
        super(Type.BBOX, field, ctx);
    }

    @Override
    public Shape read(Document doc, int docId, IndexReader reader) throws IOException {
        String bbox = doc.get(field);
        return bbox != null ? LegacyShapeReadWriterFormat.readShapeOrNull(bbox, ctx) : null;
    }

    @Override
    public void write(Shape shp, Document doc) {
        if (shp == null) {
            return;
        }

        Rectangle box = shp.getBoundingBox();
        doc.add(new StringField(field, LegacyShapeReadWriterFormat.writeShape(box), Store.YES));
    }
}
