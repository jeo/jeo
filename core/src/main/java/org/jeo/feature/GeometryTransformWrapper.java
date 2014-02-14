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
package org.jeo.feature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Feature wrapper that transforms a feature geometry.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class GeometryTransformWrapper extends FeatureWrapper {

    public GeometryTransformWrapper(Feature delegate) {
        super(delegate);
    }

    @Override
    public Geometry geometry() {
        Geometry g = super.geometry();
        return g != null ? wrap(g) : g;
    }

    @Override
    public Object get(String key) {
        Object obj = super.get(key);
        return obj instanceof Geometry ? wrap((Geometry)obj) : obj;
    }

    @Override
    public Map<String, Object> map() {
        Map<String,Object> map = new HashMap<String,Object>(super.map());
        for (Map.Entry<String, Object> e : map.entrySet()) {
            Object obj = e.getValue();
            if (obj instanceof Geometry) {
                e.setValue(wrap((Geometry)obj));
            }
        }
        return map;
    }

    @Override
    public List<Object> list() {
        List<Object> list = new ArrayList<Object>(super.list());
        for (int i = 0; i < list.size(); i++) {
            Object obj = list.get(i);
            if (obj instanceof Geometry) {
                list.set(i, wrap((Geometry)obj));
            }
        }
        return list;
    }

    protected Geometry wrap(Geometry g) {
        return g;
    }
}
