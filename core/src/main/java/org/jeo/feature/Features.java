package org.jeo.feature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.jeo.geom.Geom;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Feature utility class.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class Features {

    /** geometry factory */
    static GeometryFactory gfac = new GeometryFactory();

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
        Field geom = from.schema().geometry();
        for (Map.Entry<String, Object> kv : from.map().entrySet()) {
            String key = kv.getKey();
            Object val = kv.getValue();

            if (geom != null && geom.getName().equals(key)) {
                to.put((Geometry)val);
            }
            else {
                to.put(kv.getKey(), val);
            }
        }
        return to;
    }

    /**
     * Converts non geometry collection types in the schema to appropriate collection type.
     * 
     * @param schema The original schema.
     * 
     * @return The transformed schema.
     */
    public static Schema multify(Schema schema) {
        SchemaBuilder b = Schema.build(schema.getName());
        for (Field fld : schema) {
            if (Geometry.class.isAssignableFrom(fld.getType())) {
                Class<? extends Geometry> t = (Class<? extends Geometry>) fld.getType();
                switch(Geom.Type.from(t)) {
                case POINT:
                    t = MultiPoint.class;
                    break;
                case LINESTRING:
                    t = MultiLineString.class;
                    break;
                case POLYGON:
                    t = MultiPolygon.class;
                    break;
                }
                b.field(fld.getName(), t, fld.getCRS());
            }
            else {
                b.field(fld);
            }
        }

        return b.schema();
    }

    /**
     * Converts non collection geometry objects to associated collection type.
     * 
     * @param feature The original feature.
     * 
     * @return The transformed feature.
     */
    public static Feature multify(Feature feature) {
        return new GeometryTransformWrapper(feature) {
            @Override
            protected Geometry wrap(Geometry g) {
                switch(Geom.Type.from(g)) {
                case POINT:
                    return gfac.createMultiPoint(new Point[]{(Point)g});
                case LINESTRING:
                    return gfac.createMultiLineString(new LineString[]{(LineString)g});
                case POLYGON:
                    return gfac.createMultiPolygon(new Polygon[]{(Polygon)g});
                default:
                    return g;
                }
            }
        };
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
}
