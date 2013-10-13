package org.jeo.geojson.parser;

import java.io.IOException;

import org.jeo.geojson.simple.parser.ContentHandler;
import org.jeo.geojson.simple.parser.ParseException;

public abstract class BaseHandler implements ContentHandler {

    protected ParseContext context;
    protected Node node;

    public void init(ParseContext context, Node node) {
        this.context = context;
        this.node = node;
    }

    protected void push(String name, BaseHandler h) {
        context.push(name, h);
    }

    protected void pop() {
        context.pop();
    }

    public Node newNode(String name) {
        return context.current.newNode(name);
    }

    @Override
    public void startJSON() throws ParseException, IOException {
        unexpected();
    }
    
    @Override
    public void endJSON() throws ParseException, IOException {
        unexpected();
    }

    @Override
    public boolean startObject() throws ParseException, IOException {
        return unexpected();
    }
    
    @Override
    public boolean endObject() throws ParseException, IOException {
        return unexpected();
    }

    @Override
    public boolean startObjectEntry(String key) throws ParseException, IOException {
        return unexpected();
    }
    
    @Override
    public boolean endObjectEntry() throws ParseException, IOException {
        return unexpected();
    }

    @Override
    public boolean startArray() throws ParseException, IOException {
        return unexpected();
    }
    
    @Override
    public boolean endArray() throws ParseException, IOException {
        return unexpected();
    }
    
    @Override
    public boolean primitive(Object value) throws ParseException, IOException {
        return unexpected();
    }

    boolean unexpected() {
        if (context.isStrict()) {
            throw new IllegalStateException("Unexpected event " + context.toString());
        }
        return true;
    }
}
