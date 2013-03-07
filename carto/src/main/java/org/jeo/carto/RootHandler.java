package org.jeo.carto;

import java.util.Deque;
import java.util.Map;

import org.jeo.map.Rule;
import org.jeo.map.Stylesheet;

import com.metaweb.lessen.tokenizers.Tokenizer;
import com.metaweb.lessen.tokens.Token;

public class RootHandler extends TokenHandler {

    Stylesheet stylesheet = new Stylesheet();

    public Stylesheet getStylesheet() {
        return stylesheet;
    }

    @Override
    public TokenHandler handle(Tokenizer t, Deque<Object> stack) {

        while(t.getToken() != null) {
            Token tok = t.getToken();

            switch(tok.type) {
            case Whitespace:
                break;
            case HashName:
                return new RuleHandler();
            case Delimiter:
                String d = tok.getCleanText();
                if ("}".equals(d)) {
                    //finished selector, or map declaration
                    if (stack.peek() instanceof Rule) {
                        stylesheet.getRules().add((Rule) stack.pop());    
                    }
                    else if (stack.peek() instanceof Map) {
                        stylesheet.getProperties().putAll(((Map)stack.pop()));
                    }
                    else {
                        throw new IllegalStateException();
                    }
                }
                break;
            case Operator:
                String o = tok.getCleanText();
                if (".".equals(o)) {
                    return new RuleHandler();
                }
                throwUnexpectedToken(tok);
            case Identifier:
                if ("map".equalsIgnoreCase(tok.getCleanText())) {
                    return new MapHandler();
                }
            default:
                throwUnexpectedToken(tok);
            }

            t.next();
        }

        return null;
    }

}
