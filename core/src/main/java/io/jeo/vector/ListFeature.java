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
import io.jeo.util.Util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of Feature based on a list of values.
 * <p>
 *  Usage:
 *  <pre>
 *      Feature f = new ListFeature("id", schema, 1, 2.0, "three");
 *  </pre>
 * </p>
 * <p>
 *  List features require a schema in order to provide name mappings for values.
 * </p>
 */
public class ListFeature implements Feature {

    String id;
    List<Object> list;
    Schema schema;

    public ListFeature(Schema schema) {
        this(null, schema);
    }

    public ListFeature(String id, Schema schema) {
        this(id, schema, null);
    }

    public ListFeature(Schema schema, List<Object> values) {
        this(null, schema, values);
    }

    public ListFeature(Schema schema, Object value1, Object... values) {
        this(null, schema, value1, values);
    }

    public ListFeature(String id, Schema schema, Object value1, Object... values) {
       this(id, schema, Util.addAll(new ArrayList<>(), value1, values));
    }

    public ListFeature(String id, Schema schema, List<Object> values) {
        this.id = Features.id(id);
        this.schema = schema;
        this.list = pad(values, schema);
    }

    List<Object> pad(List<Object> values, Schema schema) {
        //copy list passed in
        values = values != null ? new ArrayList<>(values) : new ArrayList<>();

        //expand up to size of schema if necessary
        if (schema != null) {
            while(values.size() < schema.size()) {
                values.add(null);
            }
        }
        return values;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public boolean has(String key) {
        return schema.field(key) != null;
    }

    @Override
    public Object get(String key) {
        int i = schema.indexOf(key);
        return i > -1 ? list.get(i) : null;
    }

    @Override
    public Geometry geometry() {
        Field geo = schema.geometry();
        if (geo != null) {
            return (Geometry) get(geo.name());
        }

        for (Object obj : list) {
            if (obj instanceof Geometry) {
                return (Geometry) obj;
            }
        }
        return null;
    }

    @Override
    public Feature put(String key, Object val) {
        int i = schema.indexOf(key);
        if (i == -1) {
            throw new IllegalArgumentException("No such key " + key);
        }
        list.set(i, val);
        return this;
    }

    @Override
    public Feature put(Geometry g) {
        Field geo = schema.geometry();
        if (geo != null) {
            return put(geo.name(), g);
        }

        for (int i = 0; i < list.size(); i++) {
            Object obj = list.get(i);
            if (obj instanceof Geometry) {
                list.set(i, g);
                return this;
            }
        }

        throw new IllegalArgumentException("No geometry field, use put(String,Object)");
    }
    @Override
    public Map<String, Object> map() {
        LinkedHashMap<String,Object> map = new LinkedHashMap<>();
        Iterator<Field> fields = schema.iterator();
        Iterator<Object> values = list.iterator();

        while (fields.hasNext()) {
            map.put(fields.next().name(), values.next());
        }

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
