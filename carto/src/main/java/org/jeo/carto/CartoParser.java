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
