package org.jeo.carto;

import java.util.Deque;

import com.metaweb.lessen.tokenizers.Tokenizer;
import com.metaweb.lessen.tokens.Token;

public class PropertyHandler extends TokenHandler {

    @Override
    public TokenHandler handle(Tokenizer t, Deque<Object> stack) {
        while(t.getToken() != null) {
            Token tok = t.getToken();
            switch(tok.type) {
            case Whitespace:
                break;
            case Identifier:
                if (stack.peek() instanceof Property) {
                    ((Property)stack.peek()).setValue(tok.getCleanText());
                }
                else {
                    stack.push(new Property(tok.getCleanText()));
                }
                break;
            case Delimiter:
                String d = tok.getCleanText();
                if (":".equals(d)) {
                }
                else if (";".equals(d)) {
                    //finished
                    Property p = (Property) stack.pop();
                    builder(stack).set(p.getKey(), p.getValue());
                    return null;
                }
                else {
                    throwUnexpectedToken(tok);
                }
                break;
            case Number:
                String text = tok.getCleanText();
                Object value = text;
                try {
                    value = Integer.parseInt(text);
                }
                catch(NumberFormatException e) {
                    try {
                        value = Double.parseDouble(text);
                    }
                    catch(NumberFormatException e1) {
                        
                    }
                }
                ((Property)stack.peek()).setValue(value);
                break;

            case HashName:
                ((Property)stack.peek()).setValue(tok.getCleanText());
                break;
            default:
                throwUnexpectedToken(tok);
            }

            t.next();
        }
        return null;
    }

}
