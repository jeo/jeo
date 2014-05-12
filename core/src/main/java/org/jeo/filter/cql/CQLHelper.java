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
class CQLHelper extends FilterBuilder {

    String cql;

    public CQLHelper(String cql) {
        this.cql = cql;
    }

    public void intLiteral(Token token) {
        literal(Integer.parseInt(token.image)); 
    }

    public void floatLiteral(Token token) {
        literal(Double.parseDouble(token.image));
    }

    public void stringLiteral(Token token) {
        literal(dequote(token.image));
    }

    public void wktLiteral(Token token) throws ParseException {
        literal(new WKTReader().read(scan(token)));
    }

    public void fidLiteral(Token token) {
        literal(dequote(token.image));
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
        property((String)stack.pop());
    }

    public CQLHelper bbox() {
        Literal e1 = (Literal) pop();
        Literal e2 = (Literal) pop();
        Literal e3 = (Literal) pop();
        Literal e4 = (Literal) pop();

        double x1 = Convert.toNumber(e4.evaluate(null)).get().doubleValue();
        double y1 = Convert.toNumber(e3.evaluate(null)).get().doubleValue();
        double x2 = Convert.toNumber(e2.evaluate(null)).get().doubleValue();
        double y2 = Convert.toNumber(e1.evaluate(null)).get().doubleValue();

        literal(new Envelope(x1, x2, y1, y2));
        super.bbox();

        return this;
    }

    public void bboxWithSRS() {
        pop();
        bbox();
    }

}
