package org.jeo.geojson.parser;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jeo.json.parser.ParseException;

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