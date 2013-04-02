package org.jeo.carto;

import java.util.Deque;

import org.jeo.map.Selector;

import com.metaweb.lessen.tokenizers.Tokenizer;
import com.metaweb.lessen.tokens.Token;

public class RuleHandler extends TokenHandler {

    private static Object CLASS = new Object();

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
                break;
            }
            case Identifier: {
                if (stack.peek() == CLASS) {
                    stack.pop();
                    selector(stack).getClasses().add(tok.getCleanText());
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

            default:
                throw new IllegalStateException("Unexpected token: " + tok);
            }
            t.next();
        }
        return null;
    }

}
