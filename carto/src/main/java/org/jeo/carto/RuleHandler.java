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

import org.jeo.map.Selector;

import com.metaweb.lessen.tokenizers.Tokenizer;
import com.metaweb.lessen.tokens.Token;

public class RuleHandler extends TokenHandler {

    private static Object CLASS = new Object();

    private static Object COLON = new Object();

    private static Object ATTACH = new Object();
    
    @Override
    public TokenHandler handle(Tokenizer t, Deque<Object> stack) {
        while(t.getToken() != null) {
            Token tok = t.getToken();

            switch(tok.type) {
            case Whitespace:
                break;
            case HashName: {
                Selector s = selector(stack);
                s.setId(tok.getCleanText().substring(1));
                break;
            }
            case Operator:
                if (".".equals(tok.getCleanText())) {
                    stack.push(CLASS);
                }
                if ("*".equals(tok.getCleanText())) {
                    selector(stack).setWildcard(true);
                }
                break;
            case Delimiter: {
                String d = tok.getCleanText();
                if ("[".equals(d)) {
                    //start constraint
                    return new FilterHandler();
                }
                else if ("{".equals(d)) {
                    //grab selector off top of stack
                    Selector s = null;
                    if (stack.peek() instanceof Selector) {
                        s = (Selector) stack.pop();
                    }

                    builder(stack).rule().select(s);
                }
                else if ("}".equals(d)) {
                    builder(stack).endRule();
                }
                else if (":".equals(d)) {
                    if (stack.peek() == COLON) {
                        //attachment
                        stack.pop();
                        stack.push(ATTACH);
                    }
                    else {
                        stack.push(COLON);
                    }
                }
                break;
            }
            case Identifier: {
                if (stack.peek() == CLASS) {
                    stack.pop();
                    selector(stack).getClasses().add(tok.getCleanText());
                    break;
                }

                if (stack.peek() == ATTACH) {
                    stack.pop();
                    selector(stack).setAttachment(tok.getCleanText());
                    break;
                }
                if ("map".equalsIgnoreCase(tok.getCleanText())) {
                    Selector s = new Selector();
                    s.setName(tok.getCleanText());
                    stack.push(s);
                    break;
                }

                return new PropertyHandler();
            }
            case Comment:
                comment(tok);
                break;
            default:
                throw new IllegalStateException("Unexpected token: " + tok);
            }
            t.next();
        }
        return null;
    }

}
