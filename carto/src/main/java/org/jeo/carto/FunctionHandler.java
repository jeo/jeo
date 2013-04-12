package org.jeo.carto;

import java.util.Deque;

import org.jeo.filter.Function;
import org.jeo.filter.Property;

import com.metaweb.lessen.tokenizers.Tokenizer;
import com.metaweb.lessen.tokens.Token;

public class FunctionHandler extends TokenHandler {

    @Override
    public TokenHandler handle(Tokenizer t, Deque<Object> stack) {
        while(t.getToken() != null) {
            Token tok = t.getToken();
            switch(tok.type) {
            case Whitespace:
                break;
            
            case Function:
                String name = tok.getCleanText();
                if (name.endsWith("(")) {
                    name = name.substring(0, name.length()-1);
                }

                if (name.equals("randcolor")) {
                    stack.push(new RandColorFunction());
                }
                else if (name.equals("interpolate")) {
                    stack.push(new InterpolateFunction());
                }

                break;
            
            case Delimiter:
                String d = tok.getCleanText();
                if ("[".equals(d)) {
                    return new AttributeHandler();
                }
                else if ("]".equals(d)) {
                    Property p = (Property) stack.pop();
                    ((Function)stack.peek()).getArgs().add(p);
                }
                if (")".equals(d)) {
                    return null;
                }
                break;
            case Identifier:
            case String:
            case HashName:
            case Number:
                ((Function)stack.peek()).getArgs().add(expr(tok));
                break;
            default:
                throwUnexpectedToken(tok);
            }

            t.next();
        }
        return null;
    }
}
