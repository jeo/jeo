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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.jeo.json.parser.ParseException;

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
