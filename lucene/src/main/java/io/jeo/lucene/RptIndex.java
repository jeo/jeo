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
import org.apache.lucene.spatial.prefix.RecursivePrefixTreeStrategy;
import org.apache.lucene.spatial.prefix.tree.GeohashPrefixTree;
import org.apache.lucene.spatial.prefix.tree.QuadPrefixTree;
import org.apache.lucene.spatial.prefix.tree.SpatialPrefixTree;
import io.jeo.util.Key;
import io.jeo.util.Optional;

import java.util.Map;

/**
 * Spatial index based on the {@link org.apache.lucene.spatial.prefix.RecursivePrefixTreeStrategy}.
 */
public class RptIndex extends SpatialIndex<RecursivePrefixTreeStrategy> {

    public static final Key<String> FIELD = new Key<>("field", String.class);
    public static final Key<Integer> LEVELS = new Key<>("levels", Integer.class, 6);
    public static final Key<String> TREE = new Key<>("tree", String.class, "geohash");

    final RecursivePrefixTreeStrategy strategy;

    public RptIndex(JtsSpatialContext ctx, Map<String, Object> opts) {
        super(Type.RPT, ctx, opts);
        strategy = init(opts);
    }

    @Override
    public RecursivePrefixTreeStrategy strategy() {
        return strategy;
    }

    protected RecursivePrefixTreeStrategy init(Map<String, Object> opts) {
        String field = Optional.of(FIELD.get(opts)).get("field option is required");
        String grid = TREE.get(opts);
        Integer levels = LEVELS.get(opts);

        SpatialPrefixTree tree =
            "quad".equalsIgnoreCase(grid) ? new QuadPrefixTree(ctx, levels) : new GeohashPrefixTree(ctx, levels);
        return new RecursivePrefixTreeStrategy(tree, field);
    }
}
