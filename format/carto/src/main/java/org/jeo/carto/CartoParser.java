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
package org.jeo.carto;

import java.io.Reader;
import java.util.ArrayDeque;
import java.util.Deque;

import org.jeo.map.StyleBuilder;
import org.jeo.map.Style;

import com.metaweb.lessen.Utilities;
import com.metaweb.lessen.tokenizers.Tokenizer;

public class CartoParser {

    public Style parse(Reader css) {
        //raw tokenizer
        Tokenizer t = Utilities.open(css);

        //expression stack
        Deque<Object> expr = new ArrayDeque<Object>();

        //handler stack
        Deque<Object> handlers = new ArrayDeque<Object>();

        //throw a style builder on stacl
        expr.push(new StyleBuilder());

        //push root handler
        handlers.push(new RuleHandler());
        
        //go until out of tokens or no more handlers
        while(t.getToken() != null && !handlers.isEmpty()) {
            TokenHandler h = (TokenHandler) handlers.peek();
            TokenHandler n = h.handle(t, expr);
            if (n != null) {
                handlers.push(n);
            }
            else {
                handlers.pop();
            }
        }

        if (t.getToken() != null) {
            throw new IllegalStateException();
        }

        StyleBuilder builder = (StyleBuilder) expr.pop();
        return builder.style();
    }
}
