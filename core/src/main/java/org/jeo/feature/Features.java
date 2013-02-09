package org.jeo.feature;

import java.util.ArrayList;
import java.util.List;

public class Features {

    public static Feature retype(Feature feature, Schema schema) {
        List<Object> values = new ArrayList<Object>();
        for (Field f : schema) {
            values.add(feature.get(f.getName()));
        }

        return new Feature(schema, feature.getGeometry(), values);
    }
}
