package org.jeo.carto;

import static org.junit.Assert.*;

import java.util.List;

import org.jeo.cql.CQL;
import org.jeo.cql.ParseException;
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
        List<Rule> result = p.parse(css);

        assertEquals(1, result.size());
        Rule s = result.get(0);
        assertEquals("#layer", s.getName());

        assertEquals("#c00", s.get("line-color"));
        assertEquals(1, s.get("line-width"));
    }

    @Test
    public void testParseSimpleConstraint() throws ParseException {
        String css = "#layer[foo < 3] {" +
                "  line-color: #c00ffd;" + 
                "  line-width: 1;" +
                "}";

        CartoParser p = new CartoParser();
        List<Rule> result = p.parse(css);

        assertEquals(1, result.size());
        Rule s = result.get(0);
        assertEquals("#layer", s.getName());
        assertNotNull(s.getFilter());
        assertEquals(CQL.parse("foo < 3"), s.getFilter());

        assertEquals("#c00ffd", s.get("line-color"));
        assertEquals(1, s.get("line-width"));
    }

    @Test
    public void testParseAndConstraint() throws Exception {
        String css = "#layer[foo < 3][bar >= 10] {" +
                "  line-width: 1;" +
                "}";

        CartoParser p = new CartoParser();
        List<Rule> result = p.parse(css);

        assertEquals(1, result.size());
        Rule s = result.get(0);
        assertNotNull(s.getFilter());
        assertEquals(CQL.parse("foo < 3 AND bar >= 10"), s.getFilter());
    }

    @Test
    public void testParseOrConstraint() throws ParseException {
        String css = "#layer[foo < 3], [bar >= 10] {" +
                "  line-width: 1;" +
                "}";

        CartoParser p = new CartoParser();
        List<Rule> result = p.parse(css);

        assertEquals(1, result.size());
        Rule s = result.get(0);
        assertNotNull(s.getFilter());
        assertEquals(CQL.parse("foo < 3 OR bar >= 10"), s.getFilter());
    }

    @Test
    public void testParseNestedSelectors() throws ParseException {
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
        List<Rule> result = p.parse(css);

        assertEquals(1, result.size());
        assertEquals("#layer", result.get(0).getName());
        
        assertEquals(1, result.get(0).get("line-width"));
        
        assertEquals(2, result.get(0).getNested().size());

        Rule n1 = result.get(0).getNested().get(0);
        assertEquals(CQL.parse("foo < 3"), n1.getFilter());
        assertEquals("#aaa", n1.get("line-color"));

        Rule n2 = result.get(0).getNested().get(1);
        assertEquals(CQL.parse("bar >= 10"), n2.getFilter());
        assertEquals("#bbb", n2.get("line-color"));
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
