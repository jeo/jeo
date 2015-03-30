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
import com.spatial4j.core.shape.Shape;
import com.vividsolutions.jts.geom.Geometry;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;

import java.io.IOException;

/**
 * Abstraction for spatial field storage.
 */
public abstract class SpatialStorage {

    public static enum Type {
        NONE, SDV, WKT, BBOX
    }

    public static SpatialStorage create(Type type, String field, JtsSpatialContext ctx) {
        switch(type) {
            case NONE: return new NoStorage(field, ctx);
            case SDV:  return new SDVStorage(field, ctx);
            case WKT:  return new WKTStorage(field, ctx);
            case BBOX: return new BBoxStorage(field, ctx);
            default:
                throw new IllegalArgumentException("unsupported type:" + type);
        }
    }

    public final Type type;
    public final String field;
    public final JtsSpatialContext ctx;

    protected SpatialStorage(Type type, String field, JtsSpatialContext ctx) {
        this.type = type;
        this.field = field;
        this.ctx = ctx;
    }

    public abstract Shape read(Document doc, int docId, IndexReader reader)
        throws IOException;

    public abstract void write(Shape shp, Document doc);
}
