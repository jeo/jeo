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
package io.jeo.geotools;

import java.util.LinkedHashMap;
import java.util.Map;

import io.jeo.vector.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.AttributeDescriptor;

import com.vividsolutions.jts.geom.Geometry;

public class GTFeature implements Feature {

    SimpleFeature feature;

    public GTFeature(SimpleFeature feature) {
        this.feature = feature;
    }

    @Override
    public String id() {
        return feature.getID();
    }

    @Override
    public boolean has(String key) {
        return feature.getFeatureType().indexOf(key) >= 0;
    }

    @Override
    public Object get(String key) {
        return feature.getAttribute(key);
    }

    @Override
    public Geometry geometry() {
        return (Geometry) feature.getDefaultGeometry();
    }

    @Override
    public Feature put(String key, Object val) {
        feature.setAttribute(key, val);
        return this;
    }

    @Override
    public Feature put(Geometry g) {
        feature.setDefaultGeometry(g);
        return this;
    }

    @Override
    public Map<String, Object> map() {
        Map<String,Object> map = new LinkedHashMap<>();
        for (AttributeDescriptor ad : feature.getType().getAttributeDescriptors()) {
            String att = ad.getLocalName();
            map.put(att, get(att));
        }
        return map;
    }

    public SimpleFeature feature() {
        return feature;
    }
}
