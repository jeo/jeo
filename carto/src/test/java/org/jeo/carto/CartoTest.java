package org.jeo.carto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.jeo.cql.CQL;
import org.jeo.cql.ParseException;
import org.jeo.map.Rule;
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
        assertEquals("#layer", s.getName());

        assertEquals("#c00", s.get("line-color"));
        assertEquals(1, s.get("line-width"));
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
        Rule s = result.getRules().get(0);
        assertEquals("#layer", s.getName());
        assertNotNull(s.getFilter());
        assertEquals(CQL.parse("foo < 3"), s.getFilter());

        assertEquals("#c00ffd", s.get("line-color"));
        assertEquals(1, s.get("line-width"));
    }

    @Test
    public void testParseAndFilter() throws Exception {
        String css = "#layer[foo < 3][bar >= 10] {" +
                "  line-width: 1;" +
                "}";

        CartoParser p = new CartoParser();
        Stylesheet result = p.parse(css);

        assertEquals(1, result.getRules().size());
        Rule s = result.getRules().get(0);
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
        Rule s = result.getRules().get(0);
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
                "}";

        CartoParser p = new CartoParser();
        Stylesheet result = p.parse(css);

        List<Rule> rules = result.getRules();
        assertEquals(1, rules.size());
        assertEquals("#layer", rules.get(0).getName());
        
        assertEquals(1, rules.get(0).get("line-width"));
        
        assertEquals(2, rules.get(0).getNested().size());

        Rule n1 = rules.get(0).getNested().get(0);
        assertEquals(CQL.parse("foo < 3"), n1.getFilter());
        assertEquals("#aaa", n1.get("line-color"));

        Rule n2 = rules.get(0).getNested().get(1);
        assertEquals(CQL.parse("bar >= 10"), n2.getFilter());
        assertEquals("#bbb", n2.get("line-color"));
    }

    @Test
    public void testParseMap() {
        String css = 
                "Map {" + 
                "  background-color: #ffffff;" + 
                "}" + 
                "#layer {" +
                "  line-color: #c00;" + 
                "  line-width: 1;" + 
                "}";

        Stylesheet result = new CartoParser().parse(css);
        assertEquals("#ffffff", result.get("background-color"));

        assertEquals(1, result.getRules().size());
        
        Rule r = result.getRules().get(0);
        assertEquals("#layer", r.getName());
        assertEquals(Rule.Type.NAME, r.getType());
        assertEquals("#c00", r.get("line-color"));
        assertEquals(Integer.valueOf(1), r.get("line-width"));
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
        assertEquals("class", r.getName());
        assertEquals(Rule.Type.CLASS, r.getType());
        assertEquals("#c00", r.get("line-color"));
        assertEquals(Integer.valueOf(1), r.get("line-width"));
    }

    void dumpTokens(String css) {
      Tokenizer t = Utilities.open(css);
      t = new CondensingTokenizer(t, true);
      while(t.getToken() != null) {
          Token tok = t.getToken();
          System.out.println(tok.type + ", " + tok.getCleanText());
          t.next();
      }
    }
}
