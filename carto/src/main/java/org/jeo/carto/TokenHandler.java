package org.jeo.carto;

import java.util.Deque;

import com.metaweb.lessen.tokenizers.Tokenizer;
import com.metaweb.lessen.tokens.Token;

public abstract class TokenHandler {

    public abstract TokenHandler handle(Tokenizer t, Deque<Object> stack);

    protected void throwUnexpectedToken(Token t) {
        throw new IllegalStateException("Unexpected token: " + t);
    }
}
