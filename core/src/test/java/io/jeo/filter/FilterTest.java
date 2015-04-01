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
package io.jeo.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import io.jeo.vector.BasicFeature;
import io.jeo.vector.Feature;
import io.jeo.filter.cql.CQL;
import org.junit.Test;

public class FilterTest {
    
    @Test
    public void testLiteral() {
        Literal l = new Literal(12);
        assertEquals(Integer.valueOf(12), l.evaluate(null));
    }

    @Test
    public void testProperty() {
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("foo", "bar");

        Property p = new Property("foo");
        assertEquals("bar", p.evaluate(new BasicFeature(null, map)));
    }

    @Test
    public void testComparison() {
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("str", "one");
        map.put("int", 1);

        Feature f = new BasicFeature(null, map);

        Comparison c = new Comparison(Comparison.Type.EQUAL, 
            new Property("str"), new Literal("one"));
        assertTrue(c.test(f));

        c = new Comparison(Comparison.Type.EQUAL, 
                new Property("int"), new Literal(2));
        assertFalse(c.test(f));

        c = new Comparison(Comparison.Type.LESS, 
                new Property("int"), new Literal(2));
        assertTrue(c.test(f));

        c = new Comparison(Comparison.Type.LESS_OR_EQUAL, 
                new Property("int"), new Literal(1));
        assertTrue(c.test(f));

        c = new Comparison(Comparison.Type.GREATER_OR_EQUAL, 
                new Property("int"), new Literal(1));
        assertTrue(c.test(f));

        c = new Comparison(Comparison.Type.GREATER, 
                new Property("int"), new Literal(1));
        assertFalse(c.test(f));
    }

    @Test
    public void testComparisonConversion() {
        assertTrue(
            new Comparison(Comparison.Type.EQUAL, new Literal(1), new Literal(1.0)).test(null));
        assertTrue(
            new Comparison(Comparison.Type.EQUAL, new Literal("1"), new Literal(1)).test(null));
        assertTrue(
            new Comparison(Comparison.Type.EQUAL, new Literal(1.0), new Literal("1")).test(null));
    }

    @Test
    public void testLogic() {
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("str", "one");
        map.put("int", 1);

        Feature f = new BasicFeature(null, map);

        Comparison c1 = new Comparison(Comparison.Type.EQUAL, 
                new Property("str"), new Literal("one"));
        Comparison c2 = new Comparison(Comparison.Type.EQUAL, 
                new Property("int"), new Literal(1));

        Logic l = new Logic(Logic.Type.AND, c1, c2);
        assertTrue(l.test(f));
    }

    @Test
    public void testIn() {
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("str", "one");
        map.put("int", 1);

        Feature f = new BasicFeature(null, map);

        assertTrue(new In(new Property("str"), Arrays.asList(new Literal("one")), false).test(f));
        assertTrue(new In(new Property("str"), Arrays.asList(new Literal("two"), new Literal("one")), false).test(f));

        assertFalse(new In(new Property("str"), Arrays.asList(new Literal("one")), true).test(f));
        assertFalse(new In(new Property("str"), Arrays.asList(new Literal("two")), false).test(f));
    }

    @Test
    public void testLike() {
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("name", "abcdef");
        map.put("num", 123456);

        Feature f = new BasicFeature(null, map);
        assertTrue(new Like(new Property("name"), new Literal("%cd%"), false).test(f));
        assertFalse(new Like(new Property("name"), new Literal("%cd"), false).test(f));
        assertFalse(new Like(new Property("name"), new Literal("cd%"), false).test(f));
        assertTrue(new Like(new Property("name"), new Literal("cd%"), true).test(f));

        assertTrue(new Like(new Property("num"), new Literal("123%"), false).test(f));
    }

    @Test
    public void testNull() {
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("x", null);
        map.put("y", 1);

        Feature f = new BasicFeature(null, map);
        assertTrue(new Null("x", false).test(f));
        assertFalse(new Null("y", false).test(f));
        assertFalse(new Null("x", true).test(f));
        assertTrue(new Null("y", true).test(f));
        // missing properties always short-cut to false
        assertFalse(new Null("z", false).test(f));
        assertFalse(new Null("z", true).test(f));
    }

    @Test
    public void testMissingProperty() throws Exception {
        // verify the short-cut-false behavior for filters that reference a
        // missing property: 'y' in this case
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("x", 5);

        Feature f = new BasicFeature(null, map);

        // comparison
        assertFalse(CQL.parse("y < 5").test(f));
        assertFalse(CQL.parse("y = 5").test(f));
        assertFalse(CQL.parse("5 > y").test(f));
        assertFalse(CQL.parse("5 = y").test(f));

        // others
        assertFalse(CQL.parse("y in (5)").test(f));
        assertFalse(CQL.parse("y like '5'").test(f));
        assertFalse(CQL.parse("contains(y, POINT(0 0))").test(f));
        assertFalse(CQL.parse("y is null").test(f));
        assertFalse(CQL.parse("in (5)").test(f));

        // math
        assertFalse(CQL.parse("y < x + 5").test(f));
        assertFalse(CQL.parse("y + 5 < x").test(f));

        // logic
        assertTrue(CQL.parse("y > 5 or x = 5").test(f));
        assertTrue(CQL.parse("x = 5 or y > 5").test(f));
    }
}
