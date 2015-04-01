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
import java.util.List;
import java.util.Map;

import io.jeo.vector.BasicFeature;
import io.jeo.vector.Schema;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.AttributeDescriptor;

import com.vividsolutions.jts.geom.Geometry;

public class GTFeature extends BasicFeature {

    SimpleFeature feature;

    public GTFeature(final SimpleFeature feature, Schema schema) {
        super(feature.getID(), new BasicFeature.Storage(schema) {

            @Override
            protected Schema buildSchema() {
                return null;
            }

            @Override
            protected Geometry findGeometry() {
                return (Geometry) feature.getDefaultGeometry();
            }

            @Override
            protected boolean has(String key) {
                return feature.getFeatureType().indexOf(key) >= 0;
            }

            @Override
            protected Object get(String key) {
                return feature.getAttribute(key); 
            }

            @Override
            protected Object get(int index) {
                return feature.getAttribute(index); 
            }

            @Override
            protected void put(String key, Object value) {
                feature.setAttribute(key, value);
            }

            @Override
            protected void set(int index, Object value) {
                feature.setAttribute(index, value);
            }

            @Override
            protected List<Object> list() {
                return feature.getAttributes();
            }

            @Override
            protected Map<String, Object> map() {
                Map<String,Object> map = new LinkedHashMap<String, Object>();
                for (AttributeDescriptor ad : feature.getType().getAttributeDescriptors()) {
                    String att = ad.getLocalName();
                    map.put(att, get(att));
                }
                return map;
            }
            
        });
        this.feature = feature;
    }

    public SimpleFeature getFeature() {
        return feature;
    }
}
