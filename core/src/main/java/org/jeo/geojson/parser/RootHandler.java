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

import org.jeo.json.parser.ContentHandler;
import org.jeo.json.parser.ParseException;

public class RootHandler implements ContentHandler {

    ParseContext context;
    boolean finished;

    public RootHandler(BaseHandler handler) {
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

    public Object getValue() {
        return context.last.getValue();
    }
}
