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
package io.jeo.lucene;

import com.spatial4j.core.context.jts.JtsSpatialContext;
import io.jeo.util.Kvp;
import io.jeo.util.Pair;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Represents a spatial field in the lucene index.
 */
public class SpatialField {

    public static SpatialField parse(String spec, JtsSpatialContext ctx) {
        String[] split = spec.split("\\s*:\\s*");
        if (split.length < 3) {
            throw new IllegalArgumentException("input must be of the form: '<name>:<storage>:<index>[:key=val key=val ...]*'");
        }

        // field name
        String name = split[0];

        // parse storage
        SpatialStorage storage = SpatialStorage.create(
            SpatialStorage.Type.valueOf(split[1].toUpperCase(Locale.ROOT)), name, ctx);

        // parse index
        Map<String,Object> opts = new LinkedHashMap<>();
        if (split.length > 3) {
            for (Pair<String,String> kvp : Kvp.get("\\s+", "=").parse(split[3].trim())) {
                opts.put(kvp.first, kvp.second);
            }
        }
        SpatialIndex index = SpatialIndex.create(SpatialIndex.Type.valueOf(split[2].toUpperCase(Locale.ROOT)), ctx, opts);


        return new SpatialField(name, storage, index);
    }

    public final String name;
    public final SpatialStorage storage;
    public final SpatialIndex index;

    public SpatialField(String name, SpatialStorage storage, SpatialIndex index) {
        this.name = name;
        this.storage = storage;
        this.index = index;
    }
}
