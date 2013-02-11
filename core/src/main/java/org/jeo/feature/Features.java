package org.jeo.feature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Features {

    public static Feature retype(Feature feature, Schema schema) {
        List<Object> values = new ArrayList<Object>();
        for (Field f : schema) {
            values.add(feature.get(f.getName()));
        }

        return new ListFeature(values, schema);
    }

    public static MapFeature create(Map<String, Object> map) {
        return new MapFeature(map, null);
    }

    public static ListFeature create(Schema schema, Object... values) {
        return new ListFeature(Arrays.asList(values), schema);
    }
}
