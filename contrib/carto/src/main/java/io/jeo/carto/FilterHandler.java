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
package io.jeo.carto;

import java.util.Deque;

import io.jeo.filter.Filter;
import io.jeo.filter.cql.CQL;
import io.jeo.filter.cql.ParseException;
import io.jeo.map.Selector;

import com.metaweb.lessen.tokenizers.Tokenizer;
import com.metaweb.lessen.tokens.Token;

public class FilterHandler extends TokenHandler {

    static final Object OR = new Object();

    @Override
    public TokenHandler handle(Tokenizer t, Deque<Object> stack) {
        while(t.getToken() != null) {
            Token tok = t.getToken();
            switch(tok.type) {
            case Whitespace:
                if (stack.peek() instanceof StringBuilder) {
                    ((StringBuilder)stack.peek()).append(tok.getCleanText());
                }
                break;
            case Delimiter:
                String d = tok.getCleanText();
                if ("[".equals(d)) {
                    //start a new expression
                    StringBuilder b = new StringBuilder();
                    stack.push(b);
                    /*b.start();
                    stack.push(b);
                    //start a new expression, potentially anding it to the previous one
                    ExpressionBuilder b = new ExpressionBuilder();
                    if (stack.peek() instanceof Expression) {
                        b.init((Expression)stack.pop());
                    }
                    b.start();
                    stack.push(b);*/
                }
                else if ("]".equals(d)) {
                    StringBuilder b = (StringBuilder) stack.pop();
                    
                    //parse the filter
                    Filter f;
                    try {
                        f = CQL.parse(b.toString());
                    } catch (ParseException e) {
                        throw new IllegalArgumentException("Unable to parse CQL filter: " + b);
                    }
                    
                    //check the top of the stack, if Filter exists combine them
                    boolean or = false;
                    if (stack.peek() == OR) {
                        or = true;
                        stack.pop();
                    }

                    Selector s = selector(stack);
                    if (s.getFilter() != null) {
                        f = or ? s.getFilter().or(f) : s.getFilter().and(f);
                    }
                    s.setFilter(f);
                }
                else if (",".equals(d)) {
                    //add the or marker to top of stack
                    stack.push(OR);
                }
                else if ("{".equals(d)) {
                    //finished
                    return null;
                }
                else {
                    throwUnexpectedToken(tok);
                }
                break;
            case Identifier:
            case Operator:
            case Number:
            case String:
                //append expression content to builder
                ((StringBuilder) stack.peek()).append(tok.getCleanText());
                break;
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
