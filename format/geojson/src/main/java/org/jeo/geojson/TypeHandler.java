package org.jeo.geojson;

import java.io.IOException;

import org.json.simple.parser.ParseException;

public class TypeHandler extends BaseHandler {

    @Override
    public boolean primitive(Object value) throws ParseException, IOException {
        node.setValue(value);
        pop();
        return true;
    }
}
