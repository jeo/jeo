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

import org.jeo.filter.Literal;
import org.jeo.map.RGB;
import org.jeo.map.Selector;
import org.jeo.map.StyleBuilder;

import com.metaweb.lessen.tokenizers.Tokenizer;
import com.metaweb.lessen.tokens.Color;
import com.metaweb.lessen.tokens.NumericToken;
import com.metaweb.lessen.tokens.Token;

public abstract class TokenHandler {

    public abstract TokenHandler handle(Tokenizer t, Deque<Object> stack);

    protected void throwUnexpectedToken(Token t) {
        throw new IllegalStateException("Unexpected token: " + t);
    }

    protected StyleBuilder builder(Deque<Object> stack) {
        if (stack.isEmpty()) {
            throw new IllegalStateException("empty stack");
        }

        if (!(stack.peek() instanceof StyleBuilder)) {
            throw new IllegalStateException("expected StyleBuilder but was " + stack.peek().getClass());
        }

        return (StyleBuilder) stack.peek();
    }

    protected Selector selector(Deque<Object> stack) {
        if (stack.peek() instanceof Selector) {
            return (Selector) stack.peek();
        }
        stack.push(new Selector());
        return (Selector) stack.peek();
    }

    protected void comment(Token tok) {
        
    }

    protected Literal expr(Token tok) {
        if (tok instanceof Color) {
            Color col = (Color)tok;
            return new Literal(new RGB(col.getR(), col.getG(), col.getB(), 
                col.getA() != -1 ? col.getA() : 255));
        }
        else if (tok instanceof NumericToken) {
            //TODO: numeric token with units
            return new Literal(((NumericToken) tok).n);
        }
        else {
            String text = tok.getCleanText();
            return new Literal(text);
        }
    }
}
