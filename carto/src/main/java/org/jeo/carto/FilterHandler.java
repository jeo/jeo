package org.jeo.carto;

import java.util.Deque;

import org.jeo.filter.Filter;
import org.jeo.filter.cql.CQL;
import org.jeo.filter.cql.ParseException;
import org.jeo.map.Selector;

import com.metaweb.lessen.tokenizers.Tokenizer;
import com.metaweb.lessen.tokens.Token;

public class FilterHandler extends TokenHandler {

    static final Object OR = new Object();

    @Override
    public TokenHandler handle(Tokenizer t, Deque<Object> stack) {
        while(t.getToken() != null) {
            Token tok = t.getToken();
            switch(tok.type) {
            case Whitespace:
                break;
            case Delimiter:
                String d = tok.getCleanText();
                if ("[".equals(d)) {
                    //start a new expression
                    StringBuilder b = new StringBuilder();
                    stack.push(b);
                    /*b.start();
                    stack.push(b);
                    //start a new expression, potentially anding it to the previous one
                    ExpressionBuilder b = new ExpressionBuilder();
                    if (stack.peek() instanceof Expression) {
                        b.init((Expression)stack.pop());
                    }
                    b.start();
                    stack.push(b);*/
                }
                else if ("]".equals(d)) {
                    StringBuilder b = (StringBuilder) stack.pop();
                    
                    //parse the filter
                    Filter f;
                    try {
                        f = CQL.parse(b.toString());
                    } catch (ParseException e) {
                        throw new IllegalArgumentException("Unable to parse CQL filter: " + b);
                    }
                    
                    //check the top of the stack, if Filter exists combine them
                    boolean or = false;
                    if (stack.peek() == OR) {
                        or = true;
                        stack.pop();
                    }

                    Selector s = selector(stack);
                    if (s.getFilter() != null) {
                        f = or ? s.getFilter().or(f) : s.getFilter().and(f);
                    }
                    s.setFilter(f);
                }
                else if (",".equals(d)) {
                    //add the or marker to top of stack
                    stack.push(OR);
                }
                else if ("{".equals(d)) {
                    //finished
                    return null;
                }
                else {
                    throwUnexpectedToken(tok);
                }
                break;
            case Identifier:
            case Operator:
            case Number:
            case String:
                //append expression content to builder
                ((StringBuilder) stack.peek()).append(tok.getCleanText());
                break;
            case Comment:
                comment(tok);
                break;
            default:
                throwUnexpectedToken(tok);
            }
            t.next();
        }
        return null;
    }

}
