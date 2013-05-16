package org.jeo.geojson;

import java.io.IOException;

import org.json.simple.parser.ContentHandler;
import org.json.simple.parser.ParseException;

public class RootHandler implements ContentHandler {

    ParseContext context;
    boolean finished;

    RootHandler(BaseHandler handler) {
        context = new ParseContext();
        context.push(null, handler);
    }

    @Override
    public void startJSON() throws ParseException, IOException {
    }
    
    @Override
    public void endJSON() throws ParseException, IOException {
        finished = true;
    }
    
    @Override
    public boolean startObject() throws ParseException, IOException {
        return handler().startObject();
    }
    
    @Override
    public boolean endObject() throws ParseException, IOException {
        return handler().endObject();
    }
    
    @Override
    public boolean startObjectEntry(String key) throws ParseException, IOException {
        return handler().startObjectEntry(key);
    }
    
    @Override
    public boolean endObjectEntry() throws ParseException, IOException {
        return handler().endObjectEntry();
    }
    
    @Override
    public boolean startArray() throws ParseException, IOException {
        return handler().startArray();
    }
    
    @Override
    public boolean endArray() throws ParseException, IOException {
        return handler().endArray();
    }
    
    @Override
    public boolean primitive(Object value) throws ParseException, IOException {
        return handler().primitive(value);
    }

    public boolean isFinished() {
        return finished;
    }

    protected ContentHandler handler() {
        return context.handlers.peek();
    }

    Object getValue() {
        return context.last.getValue();
    }
}
