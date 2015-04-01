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
import java.util.HashMap;
import java.util.Map;

import io.jeo.json.parser.ParseException;

public class UnkownHandler extends BaseHandler {

    static Map<String,Class> HANDLERS = new HashMap<String,Class>();
    static {
        HANDLERS.put("Point", GeometryHandler.class);
        HANDLERS.put("LineString", GeometryHandler.class);
        HANDLERS.put("Polygon", GeometryHandler.class);
        HANDLERS.put("MultiPoint", GeometryHandler.class);
        HANDLERS.put("MultiLineString", GeometryHandler.class);
        HANDLERS.put("MultiPolygon", GeometryHandler.class);
        HANDLERS.put("GeometryCollection", GeometryHandler.class);
        HANDLERS.put("Feature", FeatureHandler.class);
        HANDLERS.put("FeatureCollection", FeatureCollectionHandler.class);
    }

    BaseHandler handler;

    public BaseHandler getHandler() {
        return handler;
    }

    @Override
    public boolean startObject() throws ParseException, IOException {
        return true;
    }

    @Override
    public boolean startObjectEntry(String key) throws ParseException, IOException {
        if (!"type".equals(key)) {
            throw new IllegalStateException("expected type property");
        }
        return true;
    }

    @Override
    public boolean primitive(Object value) throws ParseException, IOException {
        String type = (String) value;
        if (!HANDLERS.containsKey(type)) {
            throw new IllegalArgumentException("unknown object type: " + type);
        }

        try {
            handler = (BaseHandler) HANDLERS.get(type).newInstance();
            if (handler instanceof FeatureCollectionHandler) {
                // special case for feature collections, when parsing unknown collection we
                // can't stream
                ((FeatureCollectionHandler) handler).setStreaming(false);
            }
            push("object", handler);
        } 
        catch (Exception e) {
            throw new IOException(e);
        }
        return true;
    }

    @Override
    public boolean endObjectEntry() throws ParseException, IOException {
        return true;
    }
}
