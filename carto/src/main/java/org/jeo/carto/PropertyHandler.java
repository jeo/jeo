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

public class PropertyHandler extends TokenHandler {

    public static final Object PROPERTY = new Object();

    @Override
    public TokenHandler handle(Tokenizer t, Deque<Object> stack) {
        while(t.getToken() != null) {
            Token tok = t.getToken();
            switch(tok.type) {
            case Whitespace:
                break;
            case Identifier:
                if (stack.peek() == PROPERTY) {
                    stack.pop();
                    stack.push(new Property(tok.getCleanText()));
                }
                else if (stack.peek() instanceof Prop) {
                    ((Prop)stack.peek()).setValue(expr(tok));
                }
                else {
                    stack.push(new Prop(tok.getCleanText()));
                }
                break;
            case Delimiter:
                String d = tok.getCleanText();
                if (":".equals(d)) {
                }
                else if (";".equals(d)) {
                    //finished
                    Prop p = (Prop) stack.pop();
                    builder(stack).set(p.getKey(), p.getValue());
                    return null;
                }
                else if ("[".equals(d)) {
                    //attribute reference start
                    stack.push(PROPERTY);
                }
                else if ("]".equals(d)) {
                    //attribute reference end
                    Property att = (Property) stack.pop();
                    ((Prop)stack.peek()).setValue(att);
                }
                else if (")".equals(d) && stack.peek() instanceof Function) {
                    Function f = (Function) stack.pop();
                    ((Prop)stack.peek()).setValue(f);
                }
                else {
                    throwUnexpectedToken(tok);
                }
                break;
            case String:
            case Number:
            case HashName:
            case Dimension:
            case Color:
//                String text = tok.getCleanText();
//                Object value = text;
//                try {
//                    value = Integer.parseInt(text);
//                }
//                catch(NumberFormatException e) {
//                    try {
//                        value = Double.parseDouble(text);
//                    }
//                    catch(NumberFormatException e1) {
//                        
//                    }
//                }

                ((Prop)stack.peek()).setValue(expr(tok));
                break;

            case Function:
                return new FunctionHandler();

            case Comment:
                comment(tok);
                break;
            
            default:
                throwUnexpectedToken(tok);
            }

            t.next();
        }
        return null;
    }

    
}
