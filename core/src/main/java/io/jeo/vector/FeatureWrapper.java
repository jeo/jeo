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

import java.util.Map;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Wrapper class for {@link Feature}.
 * 
 * @author Justin Deoliveira, Boundless
 */
public class FeatureWrapper<T extends Feature> implements Feature {

    protected T delegate;

    protected FeatureWrapper(T delegate) {
        this.delegate = delegate;
    }

    public Feature getDelegate() {
        return delegate;
    }

    @Override
    public String id() {
        return delegate.id();
    }

    @Override
    public boolean has(String key) {
        return delegate.has(key);
    }

    @Override
    public Object get(String key) {
        return delegate.get(key);
    }

    @Override
    public Geometry geometry() {
        return delegate.geometry();
    }

    @Override
    public Map<String, Object> map() {
        return delegate.map();
    }

    @Override
    public Feature put(String key, Object val) {
        delegate.put(key, val);
        return this;
    }

    @Override
    public Feature put(Geometry g) {
        delegate.put(g);
        return this;
    }
}
