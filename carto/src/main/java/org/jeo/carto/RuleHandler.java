package org.jeo.carto;

import java.util.Deque;

import org.jeo.filter.Filter;
import org.jeo.map.Rule;

import com.metaweb.lessen.tokenizers.Tokenizer;
import com.metaweb.lessen.tokens.Token;

public class RuleHandler extends TokenHandler {

    @Override
    public TokenHandler handle(Tokenizer t, Deque<Object> stack) {
        while(t.getToken() != null) {
            Token tok = t.getToken();

            switch(tok.type) {
            case Whitespace:
                break;
            case HashName:
                //push the name on the stack
                stack.push(tok.getCleanText());
                break;
            case Operator:
                stack.push(Rule.Type.CLASS);
                break;
            case Delimiter:
                String d = tok.getCleanText();
                if ("[".equals(d)) {
                    //start constraint
                    return new FilterHandler();
                }
                else if ("{".equals(d)) {
                    //new selector
                    Rule s = new Rule();
                    
                    //check for constraint on stack
                    if (stack.peek() instanceof Filter) {
                        s.setFilter((Filter) stack.pop());
                    }

                    //check for a name
                    if (stack.peek() instanceof String) {
                        s.setName(stack.pop().toString());
                        s.setType(Rule.Type.NAME);
                    }

                    if (stack.peek() instanceof Rule.Type) {
                        s.setType((Rule.Type)stack.pop());
                    }
                    stack.push(s);
                }
                else if (";".equals(d)) {
                    //finished declaration
                    Property decl = (Property) stack.pop();
                    ((Rule)stack.peek()).put(decl.getKey(), decl.getValue());
                }
                else if ("}".equals(d)) {
                    //finished, check if this is a nested  or not
                    Rule s = (Rule) stack.pop();
                    if (stack.peek() instanceof Rule) {
                        //nested selector
                        ((Rule)stack.peek()).getNested().add(s);
                    }
                    else {
                        //top most selector, push back on and return
                        stack.push(s);
                        return null;
                    }

                }
                break;
            case Identifier:
                if ("map".equalsIgnoreCase(tok.getCleanText())) {
                    //do nothing
                    break;
                }

                if (stack.peek() == Rule.Type.CLASS) {
                    //start of class name
                    stack.push(tok.getCleanText());
                    break;
                }

                //start declaration
                return new PropertyHandler();
            default:
                throw new IllegalStateException("Unexpected token: " + tok);
            }
            t.next();
        }
        return null;
    }

}
