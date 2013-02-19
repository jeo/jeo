package org.jeo.carto;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import com.metaweb.lessen.tokenizers.Tokenizer;
import com.metaweb.lessen.tokens.Token;

public class RootHandler extends TokenHandler {

    List<Rule> selectors = new ArrayList<Rule>();

    public List<Rule> getSelectors() {
        return selectors;
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
                    //finished selector
                    selectors.add((Rule) stack.pop());
                }
                break;
            default:
                throwUnexpectedToken(tok);
            }

            t.next();
        }

        return null;
    }
//            case Delimiter:
//            
//                String d = tok.getCleanText();
//                
//                if ("[".equals(d)) {
//                    
//                }
//                else if (";".equals(d)) {
//                    Declaration decl = (Declaration) stack.pop();
//                    ((Selector)stack.peek()).getDeclarations().add(decl);
//                }
//                else if ("}".equals(d)) {
//                    Selector selector = (Selector) stack.pop();
//                    
//                }
//                break;
//            case HashName:
//                return new SelectorHandler();
//                if (stack.peek() instanceof Declaration) {
//                    Declaration decl = (Declaration) stack.pop();
//                    decl.setValue(tok.getCleanText());
//
//                    ((Selector)stack.peek()).getDeclarations().add(decl);
//                }
//                else {
//                    stack.push(new Selector(tok.getCleanText()));
//                }
//                
//                break;
//            case Identifier:
//                if (stack.peek() instanceof Selector) {
//                    stack.push(new Declaration(tok.getCleanText()));
//                }
//                break;
//            case Number:
//                ((Declaration)stack.peek()).setValue(tok.getCleanText());
//                break;
//            }
            
            
//        }
}
