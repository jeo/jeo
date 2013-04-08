package org.jeo.filter.cql;

import java.util.ArrayDeque;
import java.util.Deque;

import org.jeo.filter.Filter;
import org.jeo.filter.FilterBuilder;

import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

/**
 * Helper for CQL/ECQL parsing. 
 * 
 * @author Justin Deoliveira, OpenGeo
 */
class CQLHelper {

    FilterBuilder builder = new FilterBuilder(); 
    Deque<Object> stack = new ArrayDeque<Object>();
    String cql;

    public CQLHelper(String cql) {
        this.cql = cql;
    }

    public Filter filter() {
        return builder.filter();
    }

    public void intLiteral(Token token) {
        builder.literal(Integer.parseInt(token.image)); 
    }

    public void floatLiteral(Token token) {
        builder.literal(Double.parseDouble(token.image));
    }

    public void stringLiteral(Token token) {
        builder.literal(dequote(token.image));
    }

    public void wktLiteral(Token token) throws ParseException {
        builder.literal(new WKTReader().read(scan(token)));
    }

    public void fidLiteral(Token token) {
        builder.literal(dequote(token.image));
    }

    String scan(final Token t) {

        Token end = t;
        
        while (end.next != null) {
            end = end.next;
        }

        return cql.substring(t.beginColumn -1, end.endColumn);
    }

    String dequote(String str) {
        if (str.startsWith("'") && str.endsWith("'")) {
            str = str.substring(1, str.length()-1);
        }
        return str.replaceAll("''", "'");
    }

    public void idPart(Token token) {
        stack.push(token.image);
    }

    public void property() {
        builder.property((String)stack.pop());
    }

    public void and() {
        builder.and();
    }
    
    public void or() {
        builder.or();
    }
    
    public void not() {
        builder.not();
    }

    public void between() {
        builder.between();
    }

    public void eq() {
        builder.eq();
    }

    public void neq() {
        builder.neq();
    }

    public void lt() {
        builder.lt();
    }
    
    public void lte() {
        builder.lte();
    }
    
    public void gt() {
        builder.gt();
    }
    
    public void gte() {
        builder.gte();
    }

    public void intersect() {
        builder.intersect();
    }

    public void id() {
        builder.id();
    }

}
