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

import java.util.Deque;

import org.jeo.filter.Function;
import org.jeo.filter.Property;

import com.metaweb.lessen.tokenizers.Tokenizer;
import com.metaweb.lessen.tokens.Token;

public class FunctionHandler extends TokenHandler {

    @Override
    public TokenHandler handle(Tokenizer t, Deque<Object> stack) {
        while(t.getToken() != null) {
            Token tok = t.getToken();
            switch(tok.type) {
            case Whitespace:
                break;
            
            case Function:
                String name = tok.getCleanText();
                if (name.endsWith("(")) {
                    name = name.substring(0, name.length()-1);
                }

                if (name.equals("randcolor")) {
                    stack.push(new RandColorFunction());
                }
                else if (name.equals("interpolate")) {
                    stack.push(new InterpolateFunction());
                }
                else {
                    stack.push(new Function(name){
                        @Override
                        public Object evaluate(Object obj) {
                            return null;
                        }
                    });
                }
                break;
            
            case Delimiter:
                String d = tok.getCleanText();
                if ("[".equals(d)) {
                    return new AttributeHandler();
                }
                else if ("]".equals(d)) {
                    Property p = (Property) stack.pop();
                    ((Function)stack.peek()).getArgs().add(p);
                }
                if (")".equals(d)) {
                    return null;
                }
                break;
            case Identifier:
            case String:
            case HashName:
            case Number:
                ((Function)stack.peek()).getArgs().add(expr(tok));
                break;
            default:
                throwUnexpectedToken(tok);
            }

            t.next();
        }
        return null;
    }
}
