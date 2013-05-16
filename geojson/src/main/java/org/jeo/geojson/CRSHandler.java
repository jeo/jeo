package org.jeo.geojson;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.jeo.proj.Proj;
import org.json.simple.parser.ParseException;

public class CRSHandler extends BaseHandler {

    @Override
    public boolean startObject() throws ParseException, IOException {
        return true;
    }

    @Override
    public boolean endObject() throws ParseException, IOException {
        node.consume("type", String.class);
        Map<String,Object> props = node.consume("properties", Map.class).or(Collections.emptyMap());

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
