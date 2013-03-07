package org.jeo.carto;

import java.util.Deque;

import org.jeo.map.Rule;

import com.metaweb.lessen.tokenizers.Tokenizer;

public class MapHandler extends RuleHandler {

    @Override
    public TokenHandler handle(Tokenizer t, Deque<Object> stack) {
        TokenHandler h = super.handle(t, stack);
        if (h == null) {
            Rule r = (Rule) stack.pop();
            stack.push(r.getProperties());
        }
        return h;
    }
}
