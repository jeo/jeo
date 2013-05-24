package org.jeo.geojson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.parser.ParseException;

import com.vividsolutions.jts.geom.Envelope;

public class BBOXHandler extends BaseHandler {

    List<Double> values = new ArrayList<Double>();

    @Override
    public boolean startArray() throws ParseException, IOException {
        return true;
    }

    @Override
    public boolean primitive(Object value) throws ParseException, IOException {
        values.add(((Number)value).doubleValue());
        return true;
    }

    @Override
    public boolean endArray() throws ParseException, IOException {
        if (values.size() < 4) {
            throw new IllegalStateException("expected 4 values for bbox");
        }

        Envelope bbox = new Envelope(values.get(0), values.get(2), values.get(1), values.get(3));
        node.setValue(bbox);

        pop();
        return true;
    }
}
