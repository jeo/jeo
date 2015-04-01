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

import io.jeo.json.parser.ContentHandler;
import io.jeo.json.parser.ParseException;

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
