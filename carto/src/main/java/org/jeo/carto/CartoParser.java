package org.jeo.carto;

import java.util.ArrayDeque;
import java.util.Deque;

import org.jeo.map.Stylesheet;

import com.metaweb.lessen.Utilities;
import com.metaweb.lessen.tokenizers.Tokenizer;

public class CartoParser {

    public Stylesheet parse(String css) {
        //raw tokenizer
        Tokenizer t = Utilities.open(css);

        //expression stack
        Deque<Object> expr = new ArrayDeque<Object>();

        //handler stack
        Deque<Object> handlers = new ArrayDeque<Object>();

        //push root handler
        RootHandler root = new RootHandler();
        handlers.push(root);

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

        return root.getStylesheet();
    }
}
