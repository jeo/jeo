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
import org.apache.lucene.spatial.bbox.BBoxStrategy;
import org.jeo.util.Key;
import org.jeo.util.Optional;

import java.util.Map;

/**
 * Spatial index based on the {@link org.apache.lucene.spatial.bbox.BBoxStrategy}.
 */
public class BBoxIndex extends SpatialIndex<BBoxStrategy> {

    public static final Key<String> FIELD = new Key<>("prefix", String.class);

    BBoxStrategy strategy;

    public BBoxIndex(JtsSpatialContext ctx, Map<String, Object> opts) {
        super(Type.BBOX, ctx, opts);
        strategy = init(ctx, opts);
    }

    private BBoxStrategy init(JtsSpatialContext ctx, Map<String, Object> opts) {
        String prefix = Optional.of(FIELD.get(opts)).get("prefix option is required");
        return new BBoxStrategy(ctx, prefix);
    }

    @Override
    public BBoxStrategy strategy() {
        return strategy;
    }
}
