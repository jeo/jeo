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
package io.jeo.vector;

import com.vividsolutions.jts.geom.Geometry;

import java.util.HashMap;
import java.util.Map;

/**
 * Feature wrapper that tracks changes to an underlying Feature object.
 *
 * @author Justin Deoliveira, OpenGeo
 */
public class DiffFeature extends FeatureWrapper {

    Map<String,Object> changed;

    public DiffFeature(Feature feature) {
        super(feature);
        changed = new HashMap<>();
    }

    /**
     * The diff map.
     */
    public Map<String, Object> changed() {
        return changed;
    }

    /**
     * Applies the changes made to the underlying feature.
     */
    public void apply() {
        for (Map.Entry<String, Object> e: changed.entrySet()) {
            delegate.put(e.getKey(), e.getValue());
        }
        changed.clear();
    }

    public Object get(String key) {
        if (changed.containsKey(key)) {
            return changed.get(key);
        }

        return delegate.get(key);
    }

    public Feature put(String key, Object val) {
        changed.put(key, val);
        return this;
    }

    @Override
    public Feature put(Geometry g) {
        for (Map.Entry<String,Object> e : delegate.map().entrySet()) {
            if (e.getValue() instanceof Geometry) {
                changed.put(e.getKey(), g);
            }
        }
        return this;
    }
}
