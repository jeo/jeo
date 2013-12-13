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
package org.jeo.carto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.jeo.feature.BasicFeature;
import org.jeo.filter.cql.CQL;
import org.jeo.map.RGB;
import org.jeo.map.Rule;
import org.jeo.map.Selector;
import org.jeo.map.Style;
import org.junit.Test;

import com.google.common.collect.Maps;
import com.metaweb.lessen.Utilities;
import com.metaweb.lessen.tokenizers.CondensingTokenizer;
import com.metaweb.lessen.tokenizers.Tokenizer;
import com.metaweb.lessen.tokens.Token;

public class CartoTest {

    @Test
    public void testParseSimple() throws Exception {
        String css = "#layer {" +
                     "  line-color: #c00;" + 
                     "  line-width: 1;" + 
                     "}";

        Style result = Carto.parse(css);

        assertEquals(1, result.getRules().size());
        Rule s = result.getRules().get(0);
        assertEquals(1, s.getSelectors().size());
        assertEquals("layer", s.getSelectors().get(0).getId());

        assertEquals(new RGB("#c00"), s.eval("line-color", RGB.class));
        assertEquals(Integer.valueOf(1), s.eval("line-width", Integer.class));
    }

    @Test
    public void testParseSimpleFilter() throws Exception  {
        String css = "#layer[foo < 3] {" +
                "  line-color: #c00ffd;" + 
                "  line-width: 1;" +
                "}";

        Style  result = Carto.parse(css);

        assertEquals(1, result.getRules().size());

        Rule r = result.getRules().get(0);
        assertEquals(1, r.getSelectors().size());

        Selector s = r.getSelectors().get(0);
        assertEquals("layer", s.getId());
        assertNotNull(s.getFilter());
        assertEquals(CQL.parse("foo < 3"), s.getFilter());

        assertEquals("#c00ffd", r.eval("line-color", String.class));
        assertEquals(Integer.valueOf(1), r.eval("line-width", Integer.class));
    }

    @Test
    public void testParseAndFilter() throws Exception {
        String css = "#layer[foo < 3][bar >= 10] {" +
                "  line-width: 1;" +
                "}";

        Style result = Carto.parse(css);

        assertEquals(1, result.getRules().size());
        
        Rule r = result.getRules().get(0);
        assertEquals(1, r.getSelectors().size());

        Selector s = r.getSelectors().get(0);
        assertNotNull(s.getFilter());
        assertEquals(CQL.parse("foo < 3 AND bar >= 10"), s.getFilter());
    }

    @Test
    public void testParseOrFilter() throws Exception {
        String css = "#layer[foo < 3], [bar >= 10] {" +
                "  line-width: 1;" +
                "}";

        Style result = Carto.parse(css);

        assertEquals(1, result.getRules().size());

        Rule r = result.getRules().get(0);
        assertEquals(1, r.getSelectors().size());

        Selector s = r.getSelectors().get(0);
        assertNotNull(s.getFilter());
        assertEquals(CQL.parse("foo < 3 OR bar >= 10"), s.getFilter());
    }

    @Test
    public void testParseOrFilter2() throws Exception {
        String css = "#layer[foo < 3 OR bar >= 10]{" +
                "  line-width: 1;" +
                "}";
        Style result = Carto.parse(css);

        assertEquals(1, result.getRules().size());

        Rule r = result.getRules().get(0);
        assertEquals(1, r.getSelectors().size());

        Selector s = r.getSelectors().get(0);
        assertNotNull(s.getFilter());
        assertEquals(CQL.parse("foo < 3 OR bar >= 10"), s.getFilter());
    }

    @Test
    public void testParseNested() throws Exception {
        String css = 
                "#layer {" +
                "  line-width: 1;" +
                "  [foo < 3] {" +
                "    line-color: #aaa;" +
                "  }" +
                "  [bar >= 10] {" +
                "    line-color: #bbb;" +
                "  }" +
                "  [bam = 'xyz'] {" +
                "    line-color: #ccc;" +
                "  }" +
                "}";

        Style result = Carto.parse(css);

        List<Rule> rules = result.getRules();
        assertEquals(1, rules.size());

        Rule r = rules.get(0);
        assertEquals(1, r.getSelectors().size());

        Selector s = r.getSelectors().get(0);
        assertEquals("layer", s.getId());
        
        assertEquals(Integer.valueOf(1), r.eval("line-width", Integer.class));
        
        assertEquals(3, r.nested().size());

        Rule n1 = rules.get(0).nested().get(0);
        assertEquals(CQL.parse("foo < 3"), n1.getSelectors().get(0).getFilter());
        assertEquals(new RGB("#aaa"), n1.eval("line-color", RGB.class));

        Rule n2 = rules.get(0).nested().get(1);
        assertEquals(CQL.parse("bar >= 10"), n2.getSelectors().get(0).getFilter());
        assertEquals(new RGB("#bbb"), n2.eval("line-color",RGB.class));
        
        Rule n3 = rules.get(0).nested().get(2);
        assertEquals(CQL.parse("bam = 'xyz'"), n3.getSelectors().get(0).getFilter());
        assertEquals(new RGB("#ccc"), n3.eval("line-color", RGB.class));
    }

    @Test
    public void testParseMap() throws Exception {
        String css = 
                "Map {" + 
                "  background-color: rgb(0,0,0);" +  
                "}" + 
                "#layer {" +
                "  line-color: #c00;" + 
                "  line-width: 1px;" + 
                "}";

        Style result = Carto.parse(css);
        assertEquals(2, result.getRules().size());

        Rule r1 = result.getRules().get(0);
        assertEquals("Map", r1.getSelectors().get(0).getName());
        assertEquals(RGB.black, r1.eval("background-color", RGB.class));

        Rule r2 = result.getRules().get(1);
        assertEquals("layer", r2.getSelectors().get(0).getId());
        assertEquals(new RGB("#c00"), r2.eval("line-color", RGB.class));
        assertEquals(Integer.valueOf(1), r2.eval("line-width", Integer.class));
    }

    @Test
    public void testParseClass() throws Exception {
        String css = 
            ".class {" +
            "  line-color: #c00;" +
            "  line-width: 1;" + 
            "}";

        Style result = Carto.parse(css);
        Rule r = result.getRules().get(0);
        assertEquals("class", r.getSelectors().get(0).getClasses().get(0));

        assertEquals("#c00", r.eval("line-color", String.class));
        assertEquals(new RGB("#c00"), r.eval("line-color", RGB.class));

        assertEquals(Integer.valueOf(1), r.eval("line-width", Integer.class));
    }

    @Test
    public void testProperty() throws Exception {
        String css = 
            ".foo {" +
            "  line-width: [attr];" +
            "}";
        Style result = Carto.parse(css);
        
        Rule r = result.getRules().get(0);
        assertEquals("foo", r.getSelectors().get(0).getClasses().get(0));

        Map<String,Object> map = Maps.newHashMap();
        map.put("attr", "1");
        assertEquals(Integer.valueOf(1), 
            r.eval(new BasicFeature(null, map), "line-width", Integer.class));
    }

    @Test
    public void testIdentifier() throws Exception {
        String css = 
        "  #states {"+
        "    polygon-fill: #888888;"+
        "    polygon-opacity: 0.25;"+
        "    comp-op: dst;"+
        "  }";

        Style result = Carto.parse(css);
        Rule r = result.getRules().get(0);
        assertEquals("#888888", r.eval("polygon-fill", String.class));
        assertEquals(0.25, r.eval("polygon-opacity", Double.class), 0.1);
        assertEquals("dst", r.eval("comp-op", String.class));
    }

    @Test
    public void testAttachment() throws Exception {
        String css = 
            "  #states::glow {"+
            "    polygon-fill: #888888;"+
            "    polygon-opacity: 0.25;"+
            "    comp-op: dst;"+
            "  }";

        Style result = Carto.parse(css);
        
        Rule r = result.getRules().get(0);

        assertEquals(new RGB("#888888"), r.eval("polygon-fill", RGB.class));
        assertEquals(0.25, r.eval("polygon-opacity", Double.class), 0.1);
        assertEquals("dst", r.eval("comp-op", String.class));

        assertEquals(1, r.getSelectors().size());
        
        Selector s = r.getSelectors().get(0);
        assertEquals("states", s.getId());
        assertEquals("glow", s.getAttachment());
    }

    @Test
    public void testWildcard() throws Exception {
        String css = 
            "  * {"+
            "    polygon-fill: #888888;"+
            "  }";

        Style result = Carto.parse(css);
        assertEquals(1, result.getRules().size());

        Rule r = result.getRules().get(0);
        assertEquals(1, r.getSelectors().size());

        Selector s = r.getSelectors().get(0);
        assertTrue(s.isWildcard());
    }

    @Test
    public void testComments() throws Exception {
        String css = 
            "  /*" + 
            "   * some comment " +
            "   */" +
            "  * {"+
            "  /*" + 
            "   * another comment " +
            "   */" + 
            "    polygon-fill: #888888;"+
            "  }";

        Style result = Carto.parse(css);
        assertEquals(1, result.getRules().size());

        Rule r = result.getRules().get(0);
        assertEquals(1, r.getSelectors().size());

        Selector s = r.getSelectors().get(0);
        assertTrue(s.isWildcard());
    }

    @Test
    public void testFunction() throws Exception {
        String css = 
            "  * {"+
            "    polygon-fill: randcolor();"+
            "  }";

        Style result = Carto.parse(css);
        assertEquals(1, result.getRules().size());

        Rule r = result.getRules().get(0);
        assertNotNull(r.eval("polygon-fill", RGB.class));

        css = 
            "  * {"+
            "    polygon-fill: interpolate([foo], red, blue, 0, 1000);"+
            "  }";

        result = Carto.parse(css);
        assertEquals(1, result.getRules().size());

        r = result.getRules().get(0);

        Map<String,Object> map = Maps.newHashMap();
        map.put("foo", 500);

        assertNotNull(r.eval(new BasicFeature(null, map), "polygon-fill", RGB.class));
    }

    void dumpTokens(String css) {
      Tokenizer t = Utilities.open(css);
      t = new CondensingTokenizer(t, true);
      while(t.getToken() != null) {
          Token tok = t.getToken();
          System.out.println(tok.type + " " + tok.getCleanText());
          t.next();
      }
    }

    @Test
    public void testParseArrayProperty() throws Exception {
        String css = 
        "  * {"+
        "    line-dasharray: 1.0 2.0 3.0;"+
        "  }";

        Style result = Carto.parse(css);
        assertEquals(1, result.getRules().size());

        Rule r = result.getRules().get(0);
        
        Double[] arr = r.numbers(null, "line-dasharray", (Double)null);
        assertNotNull(arr);
        assertEquals(3, arr.length);

        assertEquals(1.0, arr[0], 0.1);
        assertEquals(2.0, arr[1], 0.1);
        assertEquals(3.0, arr[2], 0.1);
    }
}
