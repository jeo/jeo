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
package org.jeo.geojson.parser;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.jeo.json.parser.ParseException;
import org.jeo.proj.Proj;

public class CRSHandler extends BaseHandler {

    @Override
    public boolean startObject() throws ParseException, IOException {
        return true;
    }

    @Override
    public boolean endObject() throws ParseException, IOException {
        node.consume("type", String.class);
        Map<String,Object> props = node.consume("properties", Map.class).orElse(Collections.emptyMap());

        if (props.containsKey("name")) {
            node.setValue(Proj.crs(props.get("name").toString()));
        }

        pop();
        return true;
    }

    @Override
    public boolean startObjectEntry(String key) throws ParseException, IOException {
        if ("type".equals(key)) {
            push(key, new TypeHandler());
        }
        else if ("properties".equals(key)) {
            push(key, new PropertiesHandler());
        }
        return true;
    }

    @Override
    public boolean endObjectEntry() throws ParseException, IOException {
        return true;
    }

    @Override
    public boolean primitive(Object value) throws ParseException, IOException {
        // TODO Auto-generated method stub
        return super.primitive(value);
    }
}
