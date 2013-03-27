package org.jeo.feature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Feature utility class.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class Features {

    /**
     * Retypes a feature object to a new schema.
     * <p>
     * This method works by "pulling" the attributes defined by the fields of {@link Schema} from 
     * the feature object. 
     * </p>
     * @param feature The original feature.
     * @param schema The schema to retype to.
     * 
     * @return The retyped feature.
     */
    public static Feature retype(Feature feature, Schema schema) {
        List<Object> values = new ArrayList<Object>();
        for (Field f : schema) {
            values.add(feature.get(f.getName()));
        }

        return new ListFeature(feature.getId(), values, schema);
    }

    /**
     * Copies values from one feature to another.
     * 
     * @param from THe source feature.
     * @param to The target feature.
     * 
     * @return The target feature.
     */
    public static Feature copy(Feature from, Feature to) {
        for (Map.Entry<String, Object> kv : from.map().entrySet()) {
            to.put(kv.getKey(), kv.getValue());
        }
        return to;
    }

    /**
     * Creates a feature object from a map with an explicit schema.
     */
    public static MapFeature create(String id, Schema schema, Map<String, Object> map) {
        return new MapFeature(id, map, schema);
    }

    /**
     * Creates a feature object from a list with an explicit schema.
     */
    public static ListFeature create(String id, Schema schema, Object... values) {
        return new ListFeature(id, Arrays.asList(values), schema);
    }

    /**
     * Creates a schema object.
     * <p>
     * Example usage:
     * <pre>
     * Features.schema("cities", "loc", Point.class, "name", String.class, "pop", Integer.class)
     * </pre>
     * </p>
     * 
     * @param name The schema name.
     * @param fields A list of alternating String, Class pairs. 
     */
    public static Schema schema(String name, Object... fields) {
        if (fields.length % 2 != 0) {
            throw new IllegalArgumentException("fields must be specified as String,Class pairs");
        }

        List<Field> flds = new ArrayList<Field>();
        for (int i = 0; i < fields.length; i += 2) {
            flds.add(new Field((String) fields[i], (Class<?>)fields[i+1]));
        }

        return new Schema(name, flds);
    }
}
