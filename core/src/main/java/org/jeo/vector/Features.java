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
package org.jeo.vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.jeo.geom.Geom;
import org.jeo.proj.Proj;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;

/**
 * Feature utility class.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class Features {

    /** geometry factory */
    static GeometryFactory gfac = new GeometryFactory();

    /**
     * Returns the bounds of the feature object.
     * <p>
     * The bounds is computed by computing the aggregated bounds of all geometries of the feature 
     * object. Projections are not taken into account. To handle geometries in different projections
     * use the {@link #boundsReprojected(Feature)} method.
     * </p>
     * @param f The feature.
     * 
     * @return The bounds, or a bounds object in which {@link Envelope#isNull()} returns true.
     */
    public static Envelope bounds(Feature f) {
        Envelope e = new Envelope();
        for (Object obj : f.list()) {
            if (obj instanceof Geometry) {
                e.expandToInclude(((Geometry) obj).getEnvelopeInternal());
            }
        }
        return e;
    }

    /**
     * Returns the bounds of the feature object, reprojecting geometries of the feature if required.
     * <p>
     * The bounds is computed by computing the aggregated bounds of all geometries of the feature 
     * object. All geometries are reprojected to the crs returned from 
     * <tt>f.schema().geometry().getCRS()</tt>. Therefore this method requires that the features 
     * schema accurately represent the feature.
     * </p>
     * @param f The feature.
     * 
     * @return The bounds, or a bounds object in which {@link Envelope#isNull()} returns true.
     */
    public static Envelope boundsReprojected(Feature f) {
        Schema schema = f.schema();
        Field geo = schema.geometry();
        if (geo == null) {
            return null;
        }

        return boundsReprojected(f, geo.crs());
    }

    /**
     * Returns the bounds of the feature object, reprojecting geometries of the feature if required.
     * <p>
     * The bounds is computed by computing the aggregated bounds of all geometries of the feature 
     * object in the specified crs.
     * </p>
     * @param f The feature.
     * @param crs The target projection.
     * 
     * @return The bounds, or a bounds object in which {@link Envelope#isNull()} returns true.
     */
    public static Envelope boundsReprojected(Feature f, CoordinateReferenceSystem crs) {
        Envelope e = new Envelope();
        for (Field fld : f.schema()) {
            if (fld.geometry()) {
                CoordinateReferenceSystem c = fld.crs();
                Geometry g = (Geometry) f.get(fld.name());
                if (g == null) {
                    //ignore
                    continue;
                }

                if (c != null) {
                    g = Proj.reproject(g, c, crs);
                }
                else {
                    // no crs, just assume it is the same reference system
                }

                e.expandToInclude(g.getEnvelopeInternal());
            }
        }

        return e;
    }

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
            values.add(feature.get(f.name()));
        }

        return new BasicFeature(feature.id(), values, schema);
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

            if (geom != null && geom.name().equals(key)) {
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
        SchemaBuilder b = Schema.build(schema.name());
        for (Field fld : schema) {
            if (Geometry.class.isAssignableFrom(fld.type())) {
                Class<? extends Geometry> t = (Class<? extends Geometry>) fld.type();
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
                b.field(fld.name(), t, fld.crs());
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
                return Geom.multi(g);
            }
        };
    }

    /**
     * Creates a feature object from a map with an explicit schema.
     */
    public static Feature create(String id, Schema schema, Map<String, Object> map) {
        return new BasicFeature(id, map, schema);
    }

    /**
     * Creates a feature object from a list with an explicit schema.
     */
    public static Feature create(String id, Schema schema, Object... values) {
        return new BasicFeature(id, Arrays.asList(values), schema);
    }
}
