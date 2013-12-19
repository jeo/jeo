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
package org.jeo.map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.jeo.filter.cql.CQL;
import org.junit.Test;

public class RuleTest {

    private Style getStyle(){
        return new StyleBuilder().rule().select("#widgets").filter("cost > 12")
                .rule().select("::costly").filter("cost > 20").set("color", "yellow").endRule()
                .rule().select("::expensive").filter("cost > 30").set("color", "red").endRule()
                .set("color", "green").endRule().style();
    }

    @Test
    public void testFlattenNoNested() {
        Style style = Style.build().select("*").style();
        
        Rule r = style.getRules().get(0);
        assertEquals(1, r.flatten().size());
    }

    @Test
    public void testHashcode(){
        assertEquals(getStyle().getRules().get(0).hashCode() > 0, true);
    }

    @Test
    public void testEquals(){
        Rule r1 = getStyle().getRules().get(0);
        Rule r2 = getStyle().getRules().get(0);
        assertEquals(r1.equals(r2), true);
    }

    @Test
    public void testFlatten() throws Exception {
        Style style = getStyle();
        
        assertEquals(1, style.getRules().size());

        Rule r = style.getRules().get(0);
        assertEquals(1, r.getSelectors().size());

        Selector s = r.getSelectors().get(0);
        assertEquals("widgets", s.getId());
        assertEquals(CQL.parse("cost > 12"), s.getFilter());

        assertEquals(2, r.nested().size());

        s = r.nested().get(0).getSelectors().get(0);
        assertEquals("costly", s.getAttachment());
        assertEquals(CQL.parse("cost > 20"), s.getFilter());

        s = r.nested().get(1).getSelectors().get(0);
        assertEquals("expensive", s.getAttachment());
        assertEquals(CQL.parse("cost > 30"), s.getFilter());

        List<Rule> flat = r.flatten();
        assertEquals(3, flat.size());

        r = flat.get(0);
        assertEquals(1,r.getSelectors().size());
        s = r.getSelectors().get(0);
        assertEquals("widgets", s.getId());
        assertEquals("costly", s.getAttachment());
        assertEquals(CQL.parse("cost > 12 AND cost > 20"), s.getFilter());
        assertEquals("yellow", r.get("color"));

        r = flat.get(1);
        assertEquals(1,r.getSelectors().size());
        s = r.getSelectors().get(0);
        assertEquals("widgets", s.getId());
        assertEquals("expensive", s.getAttachment());
        assertEquals(CQL.parse("cost > 12 AND cost > 30"), s.getFilter());
        assertEquals("red", r.get("color"));

        r = flat.get(2);
        assertEquals(1,r.getSelectors().size());
        s = r.getSelectors().get(0);
        assertEquals("widgets", s.getId());
        assertNull(s.getAttachment());
        assertEquals(CQL.parse("cost > 12"), s.getFilter());
        assertEquals("green", r.get("color"));
    }
}
