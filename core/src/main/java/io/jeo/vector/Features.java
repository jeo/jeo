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
package io.jeo.vector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.jeo.geom.Geom;
import io.jeo.proj.Proj;
import io.jeo.util.Function;
import io.jeo.util.Optional;
import io.jeo.util.Util;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;

import static io.jeo.vector.VectorQuery.all;

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
        for (Object obj : f.map().values()) {
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
     * object. All geometries are reprojected to the crs returned from * <tt>f.geometry()</tt>. Therefore this method
     * requires that the features default geometry has a crs object.
     * </p>
     * @param f The feature.
     * 
     * @return The bounds, or a bounds object in which {@link Envelope#isNull()} returns true.
     */
    public static Envelope boundsReprojected(Feature f) {
        Geometry geom = f.geometry();
        CoordinateReferenceSystem crs = Proj.crs(geom);
        if (crs == null) {
            throw new IllegalArgumentException("Feature default geometry has no crs");
        }
        return boundsReprojected(f, crs);
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
        if (crs == null) {
            throw new IllegalArgumentException("crs must not be null");
        }
        Envelope e = new Envelope();
        for (Object val : f.map().values()) {
            if (val instanceof Geometry) {
                Geometry g = (Geometry) val;
                if (g == null) {
                    //ignore
                    continue;
                }

                CoordinateReferenceSystem c = Proj.crs(g);
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

        return new ListFeature(feature.id(), schema, values);
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
            String key = kv.getKey();
            Object val = kv.getValue();

            to.put(key, val);
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
        return new GeometryTransformFeature(feature) {
            @Override
            protected Geometry wrap(Geometry g) {
                return Geom.multi(g);
            }
        };
    }

    /**
     * Returns the crs of a feature.
     * <p>
     *  The crs of a feature is a crs associated with it's default geometry.
     * </p>
     *
     * @see Feature#geometry()
     * @see Proj#crs(Geometry)
     */
    public static CoordinateReferenceSystem crs(Feature f) {
        return Proj.crs(f.geometry());
    }

    /**
     * Returns a new feature id if the specified id is null.
     */
    public static String id(String id) {
        return id != null ? id : Util.uuid();
    }

    /**
     * Derives a schema for a vector dataset.
     * <p>
     * This method computes the schema by inspecting the first feature.
     * </p>
     * @param data The dataset.
     *
     * @return The optional schema.
     */
    public static Optional<Schema> schema(final VectorDataset data) throws IOException {
        return data.read(all().limit(1)).first().map(new Function<Feature, Schema>() {
            @Override
            public Schema apply(Feature f) {
                return schema(data.name(), f);
            }
        });
    }

    /**
     * Creates a schema from a feature object.
     *
     * @param name Name of the schema.
     * @param f The feature.
     *
     * @return The new schema.
     *
     * @see {@link SchemaBuilder#fields(Feature)}
     */
    public static Schema schema(String name, Feature f) {
        return Schema.build(name).fields(f).schema();
    }

    /**
     * Compares two feature objects for equality.
     * <p>
     *  Equality is based on {@link Feature#id()} and contents obtained from {@link Feature#map()}.
     * </p>
     * @param f1 The first feature.
     * @param f2 The second feature.
     *
     * @return True if the two features are equal.
     */
    public static boolean equals(Feature f1, Feature f2) {
        if (!Objects.equals(f1.id(), f2.id())) {
            return false;
        }

        return Objects.equals(f1.map(), f2.map());
    }

    /**
     * Computes the hashcode for a feature.
     * <p>
     *  To remain consistent with {@link #equals(Feature, Feature)} the hash code is computed based on
     *  {@link Feature#id()} and {@link Feature#map()}
     * </p>
     * @param f The feature.
     *
     * @return A hashcode.
     */
    public static int hashCode(Feature f) {
        return Objects.hash(f.id(), f.map());
    }

    /**
     * Returns a string representation of a feature.
     * <p>
     *  Implementations of {@link Feature} are encouraged to use this method to implement {@link Feature#toString()}.
     * </p>
     */
    public static String toString(Feature f) {
        StringBuilder sb = new StringBuilder(f.id()).append("{");
        Map<String,Object> map = f.map();
        if (!map.isEmpty()) {
            for (Map.Entry<String,Object> e : map.entrySet()) {
                sb.append(e.getKey()).append("=").append(e.getValue()).append(", ");
            }
            sb.setLength(sb.length()-2);
        }
        return sb.append("}").toString();
    }
}
