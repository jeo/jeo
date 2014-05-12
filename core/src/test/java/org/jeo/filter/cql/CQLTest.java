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

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jeo.filter.Comparison;
import org.jeo.filter.Expression;
import org.jeo.filter.Filter;
import org.jeo.filter.Id;
import org.jeo.filter.In;
import org.jeo.filter.Like;
import org.jeo.filter.Literal;
import org.jeo.filter.Logic;
import org.jeo.filter.Property;
import org.jeo.filter.Spatial;
import org.junit.Test;

public class CQLTest {

    @Test
    public void testSimpleComparison() throws ParseException {
        Filter f = CQL.parse("foo = 'bar'");
        assertTrue(f instanceof Comparison);

        Comparison c = (Comparison)f;
        assertTrue(c.getLeft() instanceof Property);
        assertTrue(c.getRight() instanceof Literal);

        Literal l = (Literal) c.getRight();
        assertEquals("bar", l.evaluate(null));
    }

    @Test
    public void testBetween() throws ParseException {
        Filter f = CQL.parse("foo BETWEEN 1 AND 10");
        assertTrue(f instanceof Logic);
        
        Logic l = (Logic)f;
        assertEquals(Logic.Type.AND, l.getType());
        assertEquals(2, l.getParts().size());

        Comparison c = (Comparison)l.getParts().get(0);
        assertEquals(Comparison.Type.GREATER_OR_EQUAL, c.getType());
        assertEquals("foo", ((Property)c.getLeft()).getProperty());
        assertEquals(1, c.getRight().evaluate(null));
        
        c = (Comparison)l.getParts().get(1);
        assertEquals(Comparison.Type.LESS_OR_EQUAL, c.getType());
        assertEquals("foo", ((Property)c.getLeft()).getProperty());
        assertEquals(10, c.getRight().evaluate(null));
    }
    
    @Test
    public void testLogic() throws ParseException {
        Filter f = CQL.parse("foo > 3 AND bar <= 10");
        assertTrue(f instanceof Logic);
    }

    @Test
    public void testSpatial() throws Exception {
        String[] ops = {
            "EQUALS",
            "DISJOINT",
            "INTERSECTS",
            "TOUCHES",
            "CROSSES",
            "WITHIN",
            "CONTAINS",
            "OVERLAPS",
            // @todo
            // "DWITHIN",
            // "BEYOND"
        };
        for (String op: ops) {
            Spatial f = (Spatial) CQL.parse(op + "(the_geom, POINT(0 0))");
            assertEquals("[the_geom] " + op + " POINT (0 0)", f.toString());
        }
    }

    @Test
    public void testLike() throws ParseException {
        Like like = (Like) CQL.parse("name LIKE 'pattern'");
        assertEquals("name", like.getProperty().getProperty());
        assertEquals("pattern", like.getPattern().pattern());
    }

    @Test
    public void testId() throws ParseException {
        Filter f = CQL.parse("IN ('foo.1', 'foo.2')");
        assertTrue(f instanceof Id);
    }

    @Test
    public void testIn() throws ParseException {
        In f = (In) CQL.parse("STATE_NAME IN ('Virginia','Maryland')");
        assertTrue(!f.isNegated());
        assertEquals("STATE_NAME", f.getProperty().getProperty());
        List<Expression> values = f.getValues();
        assertEquals("Virginia", values.get(0).evaluate(null));
        assertEquals("Maryland", values.get(1).evaluate(null));
    }

    @Test
    public void testNotIn() throws ParseException {
        In f = (In) CQL.parse("STATE_NAME NOT IN ('Virginia','Maryland')");
        assertTrue(f.isNegated());
    }

    @Test
    public void testInvalidCQL() throws ParseException {
        try {
            CQL.parse("STATE_NAME+EQ+'Virginia");
        } catch (ParseException pe) {
            assertEquals("Invalid CQL syntax: Lexical error at line 1, column 24."
                    + "  Encountered: <EOF> after : \"\\'Virginia\"", pe.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("expected to catch ParseException, got : " + ex.getClass());
        }
    }

    @Test
    public void testBBOX() throws ParseException {
        //Filter f = CQL.parse("BBOX(pp,30, -125, 40, -110)");
        //assertTrue(f instanceof Spatial);

        Filter f = CQL.parse("BBOX(pp,30, -125, 40, -110,'EPSG:4326')");
        assertTrue(f instanceof Spatial);
    }

    @Test
    public void testMaths() throws ParseException {
        Filter f = CQL.parse("UNEMPLOY / (EMPLOYED + UNEMPLOY) > .07");
        assertEquals("([UNEMPLOY] / ([EMPLOYED] + [UNEMPLOY])) > 0.07", f.toString());
    }
}
