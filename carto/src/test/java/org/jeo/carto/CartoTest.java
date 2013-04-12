package org.jeo.carto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.jeo.feature.MapFeature;
import org.jeo.filter.cql.CQL;
import org.jeo.filter.cql.ParseException;
import org.jeo.map.RGB;
import org.jeo.map.Rule;
import org.jeo.map.Selector;
import org.jeo.map.Stylesheet;
import org.junit.Test;

import com.metaweb.lessen.Utilities;
import com.metaweb.lessen.tokenizers.CondensingTokenizer;
import com.metaweb.lessen.tokenizers.Tokenizer;
import com.metaweb.lessen.tokens.Token;

public class CartoTest {

    @Test
    public void testParseSimple() {
        String css = "#layer {" +
                     "  line-color: #c00;" + 
                     "  line-width: 1;" + 
                     "}";

        CartoParser p = new CartoParser();
        Stylesheet result = p.parse(css);

        assertEquals(1, result.getRules().size());
        Rule s = result.getRules().get(0);
        assertEquals(1, s.getSelectors().size());
        assertEquals("layer", s.getSelectors().get(0).getId());

        assertEquals(new RGB("#c00"), s.eval("line-color", RGB.class));
        assertEquals(Integer.valueOf(1), s.eval("line-width", Integer.class));
    }

    @Test
    public void testParseSimpleFilter() throws ParseException {
        String css = "#layer[foo < 3] {" +
                "  line-color: #c00ffd;" + 
                "  line-width: 1;" +
                "}";

        CartoParser p = new CartoParser();
        Stylesheet  result = p.parse(css);

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

        CartoParser p = new CartoParser();
        Stylesheet result = p.parse(css);

        assertEquals(1, result.getRules().size());
        
        Rule r = result.getRules().get(0);
        assertEquals(1, r.getSelectors().size());

        Selector s = r.getSelectors().get(0);
        assertNotNull(s.getFilter());
        assertEquals(CQL.parse("foo < 3 AND bar >= 10"), s.getFilter());
    }

    @Test
    public void testParseOrFilter() throws ParseException {
        String css = "#layer[foo < 3], [bar >= 10] {" +
                "  line-width: 1;" +
                "}";

        CartoParser p = new CartoParser();
        Stylesheet result = p.parse(css);

        assertEquals(1, result.getRules().size());

        Rule r = result.getRules().get(0);
        assertEquals(1, r.getSelectors().size());

        Selector s = r.getSelectors().get(0);
        assertNotNull(s.getFilter());
        assertEquals(CQL.parse("foo < 3 OR bar >= 10"), s.getFilter());
    }

    @Test
    public void testParseNested() throws ParseException {
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

        CartoParser p = new CartoParser();
        Stylesheet result = p.parse(css);

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
    public void testParseMap() {
        String css = 
                "Map {" + 
                "  background-color: rgb(0,0,0);" +  
                "}" + 
                "#layer {" +
                "  line-color: #c00;" + 
                "  line-width: 1px;" + 
                "}";

        Stylesheet result = new CartoParser().parse(css);
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
    public void testParseClass() {
        String css = 
            ".class {" +
            "  line-color: #c00;" +
            "  line-width: 1;" + 
            "}";

        Stylesheet result = new CartoParser().parse(css);
        Rule r = result.getRules().get(0);
        assertEquals("class", r.getSelectors().get(0).getClasses().get(0));

        assertEquals("#c00", r.eval("line-color", String.class));
        assertEquals(new RGB("#c00"), r.eval("line-color", RGB.class));

        assertEquals(Integer.valueOf(1), r.eval("line-width", Integer.class));
    }

    @Test
    public void testProperty() {
        String css = 
            ".foo {" +
            "  line-width: [attr];" +
            "}";
        Stylesheet result = new CartoParser().parse(css);
        
        Rule r = result.getRules().get(0);
        assertEquals("foo", r.getSelectors().get(0).getClasses().get(0));

        assertEquals(Integer.valueOf(1), 
            r.eval(MapFeature.create("attr", "1"), "line-width", Integer.class));
    }

    @Test
    public void testIdentifier() {
        String css = 
        "  #states {"+
        "    polygon-fill: #888888;"+
        "    polygon-opacity: 0.25;"+
        "    comp-op: dst;"+
        "  }";

        Stylesheet result = new CartoParser().parse(css);
        Rule r = result.getRules().get(0);
        assertEquals("#888888", r.eval("polygon-fill", String.class));
        assertEquals(0.25, r.eval("polygon-opacity", Double.class), 0.1);
        assertEquals("dst", r.eval("comp-op", String.class));
    }

    @Test
    public void testAttachment() {
        String css = 
            "  #states::glow {"+
            "    polygon-fill: #888888;"+
            "    polygon-opacity: 0.25;"+
            "    comp-op: dst;"+
            "  }";

        Stylesheet result = new CartoParser().parse(css);
        
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

        Stylesheet result = new CartoParser().parse(css);
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

        Stylesheet result = new CartoParser().parse(css);
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

        Stylesheet result = new CartoParser().parse(css);
        assertEquals(1, result.getRules().size());

        Rule r = result.getRules().get(0);
        assertNotNull(r.eval("polygon-fill", RGB.class));

        css = 
            "  * {"+
            "    polygon-fill: interpolate([foo], red, blue, 0, 1000);"+
            "  }";

        result = new CartoParser().parse(css);
        assertEquals(1, result.getRules().size());

        r = result.getRules().get(0);
        assertNotNull(r.eval(MapFeature.create("foo", 500), "polygon-fill", RGB.class));
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
}
