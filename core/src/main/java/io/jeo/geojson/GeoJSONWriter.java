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
package io.jeo.geojson;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import io.jeo.data.Cursor;
import io.jeo.geom.Geom;
import io.jeo.vector.Feature;
import io.jeo.json.encoder.JSONEncoder;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;

/**
 * Writes out GeoJSON objects as defined by {@linkplain http://www.geojson.org/geojson-spec.html}.
 * <p>
 * Example:
 * <pre><code>
 * Writer w = ...;
 * Point p = ...;
 * 
 * GeoJSONWriter writer = new GeoJSONWriter(w);
 * writer.object()
 * writer.key("location").point(p);
 * writer.endObject();
 * </code></pre>
 * </p>
 * @author Justin Deoliveira, OpenGeo
 */
public class GeoJSONWriter extends JSONEncoder {

    /**
     * Encodes a geometry object to a GeoJSON string.
     *  
     * @param g The geometry.
     * 
     * @return A GeoJSON geometry string.
     * 
     * @see {@linkplain http://www.geojson.org/geojson-spec.html#geometry-objects}
     */
    public static String toString(Geometry g) {
        StringWriter w = new StringWriter();
        try {
            new GeoJSONWriter(w).geometry(g);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return w.toString();
    }

    /**
     * Encodes a feature object to a GeoJSON string.
     *  
     * @param f The feature.
     * 
     * @return A GeoJSON feature string.
     * 
     * @see {@linkplain http://www.geojson.org/geojson-spec.html#feature-objects}
     */
    public static String toString(Feature f) {
        StringWriter w = new StringWriter();
        try {
            new GeoJSONWriter(w).feature(f);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return w.toString();
    }

    /**
     * Encodes a feature cursor object to a GeoJSON string.
     *  
     * @param features The feature cursor.
     * 
     * @return A GeoJSON feature collection string.
     * 
     * @see {@linkplain http://www.geojson.org/geojson-spec.html#feature-collection-objects}
     */
    public static String toString(Cursor<Feature> features) throws IOException {
        StringWriter w = new StringWriter();
        new GeoJSONWriter(w).featureCollection(features);
        return w.toString();
    }

    /**
     * Creates a new writer.
     * 
     * @param out The writer to encode to.
     */
    public GeoJSONWriter(Writer out) {
        super(out);
    }

    /**
     * Creates a new writer with formating.
     * 
     * @param out The writer to encode to.
     * @param indentSize The number of spaces to use when indenting.
     */
    public GeoJSONWriter(Writer out, int indentSize) {
        super(out, indentSize);
    }

    
    /**
     * Encodes a bounding box as a 4 element array.
     * 
     * @param b The The bounding box.
     * @see {@linkplain http://www.geojson.org/geojson-spec.html#bounding-boxes}
     */
    public GeoJSONWriter bbox(Envelope b) throws IOException {
        array()
            .value(b.getMinX()).value(b.getMinY()).value(b.getMaxX()).value(b.getMaxY())
            .endArray();
        return this;
    }

    /**
     * Encodes a crs as a named crs.
     * 
     * @param crs The crs object.
     * @see {@linkplain http://www.geojson.org/geojson-spec.html#named-crs}
     */
    public GeoJSONWriter crs(CoordinateReferenceSystem crs)  throws IOException {
        if (crs == null) {
            nul();
            return this;
        }

        object()
          .key("type").value("name")
          .key("properties").object()
            .key("name").value(crs.getName())
            .endObject()
           .endObject();
        return this;
    }

    // geometry methods
    //
    /**
     * Encodes a geometry object.
     * 
     * @param g The geometry.
     * 
     * @see {@linkplain http://www.geojson.org/geojson-spec.html#geometry-objects}
     */
    public GeoJSONWriter geometry(Geometry g)  throws IOException {
        if (g == null) {
            return (GeoJSONWriter) nul();
        }
    
        switch (Geom.Type.from(g)) {
        case POINT:
            return point((Point) g);
        case LINESTRING:
            return lineString((LineString) g);
        case POLYGON:
            return polygon((Polygon) g);
        case MULTIPOINT:
            return multiPoint((MultiPoint) g);
        case MULTILINESTRING:
            return multiLineString((MultiLineString) g);
        case MULTIPOLYGON:
            return multiPolygon((MultiPolygon) g);
        case GEOMETRYCOLLECTION:
            return geometryCollection((GeometryCollection) g);
        }
        throw new IllegalArgumentException("Unable to encode " + g + " as GeoJSON");
    }

    /**
     * Encodes a point object.
     * 
     * @param p The point.
     * 
     * @see {@linkplain http://www.geojson.org/geojson-spec.html#point}
     */
    public GeoJSONWriter point(Point p) throws IOException {
        if (p == null) {
            return (GeoJSONWriter) nul();
        }

        object()
          .key("type").value("Point")
          .key("coordinates").array()
              .value(p.getX())
              .value(p.getY());
        
        if (!Double.isNaN(p.getCoordinate().z)) {
            value(p.getCoordinate().z);
        }

        endArray().endObject();

        return this;
    }

    /**
     * Encodes a linestring object.
     * 
     * @param l The linestring.
     * 
     * @see {@linkplain http://www.geojson.org/geojson-spec.html#linestring}
     */
    public GeoJSONWriter lineString(LineString l) throws IOException {
        if (l == null) {
            return (GeoJSONWriter) nul();
        }

        object()
          .key("type").value("LineString")
          .key("coordinates").array(l.getCoordinateSequence())
          .endObject();

        return this;
    }

    /**
     * Encodes a polygon object.
     * 
     * @param p The polygon
     * 
     * @see {@linkplain http://www.geojson.org/geojson-spec.html#polygon}
     */
    public GeoJSONWriter polygon(Polygon p) throws IOException {
        if (p == null) {
            return (GeoJSONWriter) nul();
        }

        object()
          .key("type").value("Polygon")
          .key("coordinates").array(p)
          .endObject();

        return this;
    }
    
    /**
     * Encodes a multipoint object.
     * 
     * @param mp The multipoint
     * 
     * @see {@linkplain http://www.geojson.org/geojson-spec.html#multipoint}
     */
    public GeoJSONWriter multiPoint(MultiPoint mp) throws IOException {
        if (mp == null) {
            return (GeoJSONWriter) nul();
        }

        object()
          .key("type").value("MultiPoint")
          .key("coordinates").array(mp.getCoordinates())
          .endObject();

        return this;
    }

    /**
     * Encodes a multilinestring object.
     * 
     * @param ml The multilinestring
     * 
     * @see {@linkplain http://www.geojson.org/geojson-spec.html#multilinestring}
     */
    public GeoJSONWriter multiLineString(MultiLineString ml) throws IOException {
        if (ml == null) {
            return (GeoJSONWriter) nul();
        }

        object()
          .key("type").value("MultiLineString")
          .key("coordinates");
        
        array();
        for (LineString ls : Geom.iterate(ml)) {
            array(ls.getCoordinateSequence());
        }
        endArray();

        endObject();

        return this;
    }

    /**
     * Encodes a multipolygon object.
     * 
     * @param mp The multipolygon
     * 
     * @see {@linkplain http://www.geojson.org/geojson-spec.html#multipolygon}
     */
    public GeoJSONWriter multiPolygon(MultiPolygon mp) throws IOException {
        if (mp == null) {
            return (GeoJSONWriter) nul();
        }

        object()
          .key("type").value("MultiPolygon")
          .key("coordinates");

        array();
        for (Polygon p : Geom.iterate(mp)) {
            array(p);
        }
        endArray();

        endObject();

        return this;
    }

    /**
     * Encodes a geometry collection object.
     * 
     * @param gc The geometry collection.
     * 
     * @see {@linkplain http://www.geojson.org/geojson-spec.html#geometry-collection}
     */
    public GeoJSONWriter geometryCollection(GeometryCollection gc) throws IOException {
        if (gc == null) {
            return (GeoJSONWriter) nul();
        }

        object()
          .key("type").value("GeometryCollection")
          .key("geometries");

        array();

        for (Geometry g : Geom.iterate(gc)) {
            geometry(g);
        }

        endArray();
        endObject();

        return this;

    }

    // feature methods
    /**
     * Encodes a feature object.
     * 
     * @param f The feature.
     * 
     * @see {@linkplain http://www.geojson.org/geojson-spec.html#feature-objects}
     */
    public GeoJSONWriter feature(Feature f)  throws IOException {
        if (f == null) {
            nul();
            return this;
        }

        object()
            .key("type").value("Feature");

        if (f.id() != null) {
            key("id").value(f.id());
        }

        Geometry g = f.geometry();
        if (g != null) {
            key("geometry").geometry(g);
        }

        key("properties").object();
        for (Map.Entry<String, Object> p : f.map().entrySet()) {
            String key = p.getKey();
            Object o = p.getValue();
            if (o instanceof Geometry) {
                 if (o == g) {
                     continue;
                 }
    
                 key(key).geometry((Geometry)o);
            }
            else {
                key(key).value(o);
            }
        }
        endObject();

        endObject();

        return this;
    }

    /**
     * Encodes a feature collection.
     *
     * @see {@linkplain http://www.geojson.org/geojson-spec.html#feature-collection-objects}
     */
    public GeoJSONWriter featureCollection(Cursor<Feature> features)  throws IOException {
        featureCollection();

        for (Feature f : features) {
            feature(f);
        }

        return endFeatureCollection();
    }

    /**
     * Starts the encoding of a feature collection.
     * <p>
     * This method should be followed by any number of calls to {@link #feature(Feature)} and 
     * finally terminated with a call to {@link #endFeatureCollection()}.
     * </p> 
     * 
     * @see {@linkplain http://www.geojson.org/geojson-spec.html#feature-collection-objects}
     */
    public GeoJSONWriter featureCollection() throws IOException {
        object()
            .key("type").value("FeatureCollection")
            .key("features").array();
        return this;
    }

    /**
     * Finishes the encoding a feature collection previously started with 
     * {@link #featureCollection()}.
     */
    public GeoJSONWriter endFeatureCollection() throws IOException {
        return (GeoJSONWriter) endArray().endObject();
    }

    // override for type narrowing
    //
    
    @Override
    public GeoJSONWriter object() throws IOException {
        return (GeoJSONWriter) super.object();
    }

    @Override
    public GeoJSONWriter array() throws IOException {
        return (GeoJSONWriter) super.array();
    }

    @Override
    public GeoJSONWriter key(String key) throws IOException {
        return (GeoJSONWriter) super.key(key);
    }

    @Override
    public GeoJSONWriter value(Number value) throws IOException {
        return (GeoJSONWriter) super.value(value);
    }

    @Override
    public GeoJSONWriter value(Object value) throws IOException {
        return (GeoJSONWriter) super.value(value);
    }

    @Override
    public GeoJSONWriter nul() throws IOException {
        return (GeoJSONWriter) super.nul();
    }

    @Override
    public GeoJSONWriter value(String value) throws IOException {
        return (GeoJSONWriter) super.value(value);
    }

    @Override
    public GeoJSONWriter endObject() throws IOException {
        return (GeoJSONWriter) super.endObject();
    }

    @Override
    public GeoJSONWriter endArray() throws IOException {
        return (GeoJSONWriter) super.endArray();
    }

    @Override
    public GeoJSONWriter flush() throws IOException {
        return (GeoJSONWriter) super.flush();
    }

    GeoJSONWriter array(Coordinate[] coords) throws IOException {
        return array(new CoordinateArraySequence(coords));
    }

    GeoJSONWriter array(CoordinateSequence coordseq) throws IOException {
        array();
        
        int dim = coordseq.getDimension();
        for (int i = 0; i < coordseq.size(); i++) {
            array()
                .value(coordseq.getOrdinate(i, 0))
                .value(coordseq.getOrdinate(i, 1));
            
            if (dim > 2) {
                double v = coordseq.getOrdinate(i, 2);
                if (!Double.isNaN(v)) {
                    value(v);
                }
            }
            endArray();
        }

        endArray();
        return this;
    }

    GeoJSONWriter array(Polygon p) throws IOException {
        array();
        array(p.getExteriorRing().getCoordinateSequence());
        for (int i = 0; i < p.getNumInteriorRing(); i++) {
            array(p.getInteriorRingN(i).getCoordinateSequence());
        }
        endArray();
        return this;
    }
}
