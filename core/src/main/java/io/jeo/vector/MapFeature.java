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
package io.jeo.vector;

import com.vividsolutions.jts.geom.Geometry;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Implementation of Feature based on a map of values.
 */
public class MapFeature implements Feature {

    String id;
    Map<String,Object> map;

    public MapFeature(String id) {
        this(id, new LinkedHashMap<String, Object>());
    }

    public MapFeature(Map<String,Object> map) {
        this(null, map);
    }

    public MapFeature(String id, Map<String,Object> map) {
        this.id = Features.id(id);
        this.map = map;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public boolean has(String key) {
        return map.containsKey(key);
    }

    @Override
    public Object get(String key) {
        return map.get(key);
    }

    @Override
    public Geometry geometry() {
        for (Object val : map.values()) {
            if (val instanceof Geometry) {
                return (Geometry) val;
            }
        }
        return null;
    }

    @Override
    public Feature put(String key, Object val) {
        map.put(key, val);
        return this;
    }

    @Override
    public Feature put(Geometry g) {
        for (Map.Entry<String,Object> kv : map.entrySet()) {
            if (kv.getValue() instanceof Geometry) {
                kv.setValue(g);
                break;
            }
        }
        return this;
    }

    @Override
    public Map<String, Object> map() {
        return map;
    }

    @Override
    public String toString() {
        return Features.toString(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Feature) {
            return Features.equals(this, (Feature) obj);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Features.hashCode(this);
    }
}
