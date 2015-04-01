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
package io.jeo.geojson.parser;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;

import io.jeo.json.parser.ParseException;

/**
 * TODO: handle nested objects and arrays
 */
class PropertiesHandler extends BaseHandler {

    String key;
    Deque<Map<String,Object>> stack = new ArrayDeque<Map<String,Object>>();

    @Override
    public boolean startObject() throws ParseException, IOException {
        Map<String,Object> map = new LinkedHashMap<String, Object>();

        //top level?
        if (!stack.isEmpty()) {
            stack.peek().put(key, map);
        }

        stack.push(map);
        return true;
    }

    @Override
    public boolean endObject() throws ParseException, IOException {
        Map<String,Object> map = stack.pop();
        if (stack.isEmpty()) {
            node.setValue(map);
            pop();
        }

        return true;
    }

    @Override
    public boolean startObjectEntry(String key) throws ParseException,
            IOException {
        this.key = key;
        return true;
    }

    @Override
    public boolean primitive(Object value) throws ParseException, IOException {
        stack.peek().put(key, value);
        return true;
    }

    @Override
    public boolean endObjectEntry() throws ParseException, IOException {
        return true;
    }
}
