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
import com.spatial4j.core.shape.jts.JtsGeometry;
import com.vividsolutions.jts.geom.Geometry;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.SlowCompositeReaderWrapper;
import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.spatial.serialized.SerializedDVStrategy;

import java.io.IOException;

public class SDVStorage extends SpatialStorage {

    SerializedDVStrategy strategy;
    ValueSource values;

    public SDVStorage(String fieldName, JtsSpatialContext ctx) {
        super(Type.SDV, fieldName, ctx);
        strategy = new SerializedDVStrategy(ctx, fieldName);
        values = strategy.makeShapeValueSource();
    }

    @Override
    public Geometry read(Document doc, int docId, IndexReader reader)
        throws IOException {
        FunctionValues v = values.getValues(null, SlowCompositeReaderWrapper.wrap(reader).getContext());
        JtsGeometry shp = (JtsGeometry) v.objectVal(docId);
        return shp.getGeom();
    }
}
