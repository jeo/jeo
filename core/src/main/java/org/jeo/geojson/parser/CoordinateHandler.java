package org.jeo.geojson.parser;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.jeo.geojson.simple.parser.ParseException;

public class CoordinateHandler extends BaseHandler {

    int depth = 0;
    Deque<List> stack = new ArrayDeque<List>();

    @Override
    public boolean startArray() throws ParseException, IOException {
        stack.push(new ArrayList());
        depth++;
        return true;
    }

    @Override
    public boolean endArray() throws ParseException, IOException {
        depth--;

        List l = stack.pop();

        if (depth == 0) {
            node.setValue(l);
            pop();
        }
        else {
            stack.peek().add(l);
        }
        return true;
    }
    
    @Override
    public boolean primitive(Object value) throws ParseException, IOException {
        stack.peek().add(value);
        return true;
    }

}
