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
import org.apache.lucene.spatial.SpatialStrategy;

import java.util.Map;

/**
 * Abstraction for spatial field index.
 */
public abstract class SpatialIndex<T extends SpatialStrategy> {

    public static enum Type {
        RPT, BBOX
    }

    public static SpatialIndex<?> create(Type type, JtsSpatialContext ctx, Map<String,Object> opts) {
        switch(type) {
            case RPT:  return new RptIndex(ctx, opts);
            case BBOX: return new BBoxIndex(ctx, opts);
        }
        throw new IllegalArgumentException("No such type: " + type);
    }

    public final Type type;
    public final JtsSpatialContext ctx;

    protected SpatialIndex(Type type, JtsSpatialContext ctx, Map<String,Object> opts) {
        this.type = type;
        this.ctx = ctx;
    }

    public abstract T strategy();
}
