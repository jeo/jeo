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
package org.jeo.geojson;

import java.io.IOException;
import java.io.Reader;
import java.util.Collection;

import org.jeo.data.Cursor;
import org.jeo.data.Cursors;
import org.jeo.vector.Feature;
import org.jeo.geojson.parser.BaseHandler;
import org.jeo.geojson.parser.FeatureCollectionHandler;
import org.jeo.geojson.parser.FeatureHandler;
import org.jeo.geojson.parser.GeometryHandler;
import org.jeo.geojson.parser.RootHandler;
import org.jeo.geojson.parser.UnkownHandler;
import org.jeo.json.parser.JSONParser;
import org.jeo.json.parser.ParseException;
import org.jeo.util.Convert;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import org.jeo.vector.FeatureCursor;

/**
 * GeoJSON reader class.
 * <p>
 * See {@linkplain http://www.geojson.org/geojson-spec.html}.
 * </p>
 * <p>
 * Methods of this class take any input that be converted to a {@link Reader}. See the 
 * {@link Convert#toReader(Object)} method for details on accepted inputs. 
 * </p>
 * <p>
 * Example:
 * <pre><code>
 * GeoJSONReader reader = new GeoJSONReader();
 * reader.read("{ 'type': 'Point', coordinates: [1.0, 2.0] }");
 * </code></pre>
 * </p>
 * @author Justin Deoliveira, Boundless
 */
public class GeoJSONReader {

    /**
     * Reads a geometry object.
     * 
     * @param json Input object, see {@link Convert#toReader(Object)}.
     * 
     * @return The geometry. 
     */
    public Geometry geometry(Object json) {
        return (Geometry) parse(json, new GeometryHandler());
    }

    /**
     * Reads a point object.
     * 
     * @param json Input object, see {@link Convert#toReader(Object)}.
     * 
     * @return The point. 
     */
    public Point point(Object json) {
        return (Point) geometry(json); 
    }

    /**
     * Reads a linestring object.
     * 
     * @param json Input object, see {@link Convert#toReader(Object)}.
     * 
     * @return The linestring. 
     */
    public LineString lineString(Object json) {
        return (LineString) geometry(json);
    }

    /**
     * Reads a polygon object.
     * 
     * @param json Input object, see {@link Convert#toReader(Object)}.
     * 
     * @return The polygon. 
     */
    public Polygon polygon(Object json) {
        return (Polygon) geometry(json);
    }

    /**
     * Reads a multipoint object.
     * 
     * @param json Input object, see {@link Convert#toReader(Object)}.
     * 
     * @return The multipoint. 
     */
    public MultiPoint multiPoint(Object json) {
        return (MultiPoint) geometry(json);
    }

    /**
     * Reads a multilinestring object.
     * 
     * @param json Input object, see {@link Convert#toReader(Object)}.
     * 
     * @return The multilinetring. 
     */
    public MultiLineString multiLineString(Object json) {
        return (MultiLineString) geometry(json);
    }

    /**
     * Reads a multipolygon object.
     * 
     * @param json Input object, see {@link Convert#toReader(Object)}.
     * 
     * @return The multipolygon. 
     */
    public MultiPolygon multiPolygon(Object json) {
        return (MultiPolygon) geometry(json);
    }

    /**
     * Reads a geometry collection object.
     * 
     * @param json Input object, see {@link Convert#toReader(Object)}.
     * 
     * @return The geometry collection. 
     */
    public GeometryCollection geometryCollection(Object json) {
        return (GeometryCollection) geometry(json);
    }

    /**
     * Reads a feature object.
     * 
     * @param json Input object, see {@link Convert#toReader(Object)}.
     * 
     * @return The feature. 
     */
    public Feature feature(Object json) {
        return (Feature) parse(json, new FeatureHandler());
    }

    /**
     * Reads a feature collection object.
     * 
     * @param json Input object, see {@link Convert#toReader(Object)}.
     * 
     * @return The feature collection as a cursor. 
     */
    public FeatureCursor features(Object json) {
        try {
            return new GeoJSONCursor(toReader(json));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reads a geojson object.
     * 
     * @param json Input object, see {@link Convert#toReader(Object)}.
     * 
     * @return The geojson object. 
     */
    public Object read(Object json) {
        UnkownHandler h = new UnkownHandler();
        Object result = parse(json, h);

        if (h.getHandler() instanceof FeatureCollectionHandler) {
            return Cursors.create((Collection<Feature>)result);
        }

        return result;
    }

    Reader toReader(Object input) throws IOException {
        return Convert.toReader(input).get("unable to turn " + input + " into reader");
    }

    Object parse(Object input, BaseHandler handler) {
        try {
            return parse(toReader(input), handler);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    Object parse(Reader input, BaseHandler handler) throws IOException {
        JSONParser p = new JSONParser();
        RootHandler h = new RootHandler(handler);
        try {
            p.parse(input, h);
            return h.getValue();
        } catch (ParseException e) {
            throw new IOException(e);
        }
    }
}
