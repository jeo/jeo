package org.jeo.geojson;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Before;

public class GeoJSONTestSupport {

    protected GeoJSONReader reader;

    @Before
    public void setUp() {
        reader = new GeoJSONReader();
    }

    protected String strip(String json) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == ' ' || c == '\n') continue;
            if (c == '\'') {
                sb.append("\"");
            }
            else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    protected String toString(int val) {
        return val == 0 ? "zero" : 
            val == 1 ? "one" :
            val == 2 ? "two" : 
            val == 3 ? "three" : "four";
    }

    protected StringReader reader(String json) throws IOException {
        return new StringReader(json);
    }

}
