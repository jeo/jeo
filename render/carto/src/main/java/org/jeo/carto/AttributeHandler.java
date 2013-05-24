package org.jeo.carto;

import java.util.Deque;

import org.jeo.filter.Property;

import com.metaweb.lessen.tokenizers.Tokenizer;
import com.metaweb.lessen.tokens.Token;

public class AttributeHandler extends TokenHandler {

    @Override
    public TokenHandler handle(Tokenizer t, Deque<Object> stack) {
        while(t.getToken() != null) {
            Token tok = t.getToken();
            switch(tok.type) {
            case Whitespace:
                break;
    
            case Delimiter:
                String d = tok.getCleanText();
                if ("]".equals(d)) {
                    return null;
                }
                break;
            
            case Identifier:
                stack.push(new Property(tok.getCleanText()));
                break;
            default:
                throwUnexpectedToken(tok);
            }

            t.next();
        }
        return null;
    }

}
