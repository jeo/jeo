/* Copyright 2013 The jeo project. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jeo.filter.cql;

import java.util.ArrayDeque;
import java.util.Deque;

import org.jeo.filter.Filter;
import org.jeo.filter.FilterBuilder;
import org.jeo.filter.Literal;
import org.jeo.util.Convert;

import com.vividsolutions.jts.geom.Envelope;
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

    public void like() {
        builder.like();
    }

    public void notLike() {
        builder.notLike();
    }

    public void equals() {
        builder.equals();
    }

    public void intersect() {
        builder.intersect();
    }

    public void touch() {
        builder.touch();
    }

    public void disjoint() {
        builder.disjoint();
    }

    public void overlap() {
        builder.overlap();
    }

    public void cross() {
        builder.cross();
    }

    public void cover() {
        builder.cover();
    }

    public void within() {
        builder.within();
    }

    public void contain() {
        builder.contain();
    }

    public void bbox() {
        Literal e1 = (Literal) builder.pop();
        Literal e2 = (Literal) builder.pop();
        Literal e3 = (Literal) builder.pop();
        Literal e4 = (Literal) builder.pop();

        double x1 = Convert.toNumber(e4.evaluate(null)).get().doubleValue();
        double y1 = Convert.toNumber(e3.evaluate(null)).get().doubleValue();
        double x2 = Convert.toNumber(e2.evaluate(null)).get().doubleValue();
        double y2 = Convert.toNumber(e1.evaluate(null)).get().doubleValue();

        builder.literal(new Envelope(x1, x2, y1, y2));
        builder.bbox();
    }

    public void bboxWithSRS() {
        builder.pop();
        bbox();
    }

    public void id() {
        builder.id();
    }

    public void in() {
        builder.in();
    }

    public void notIn() {
        builder.notIn();
    }

    public void pop() {
        builder.pop();
    }
}
