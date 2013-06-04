package org.jeo.geojson;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jeo.data.Cursor;
import org.jeo.feature.Feature;
import org.jeo.geom.Geom;
import org.json.simple.JSONArray;
import org.json.simple.JSONStreamAware;
import org.json.simple.JSONValue;
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
 *  
 * @author Justin Deoliveira, OpenGeo
 */
public class GeoJSONWriter {

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
     * Creates a bounding box GeoJSON object.
     * 
     * @param bbox The bounding box.
     * 
     * @return A GeoJSON object as defined by {@linkplain http://www.geojson.org/geojson-spec.html#nbounding-boxes}
     */
    public static List toObject(Envelope bbox) {
        JSONArray list = new JSONArray();
        list.add(bbox.getMinX());
        list.add(bbox.getMinY());
        list.add(bbox.getMaxX());
        list.add(bbox.getMaxY());
        return list;
    }

    /**
     * Creates a crs GeoJSON object.
     * 
     * @param crs The crs.
     * 
     * @return A GeoJSON object as defined by {@linkplain http://www.geojson.org/geojson-spec.html#named-crs}
     */
    public static Map<String,Object> toObject(CoordinateReferenceSystem crs) {
        if (crs == null) {
            return null;
        }
        Map<String,Object> obj = object();
        
        obj.put("type", "name");

        Map<String,Object> props = object();
        props.put("name", crs.getName());

        obj.put("properties", props);
        return obj;
    }

    /**
     * Creates a point GeoJSON object.
     * 
     * @param p The point.
     * 
     * @return A GeoJSON object as defined by {@linkplain http://www.geojson.org/geojson-spec.html#point}
     */
    public static Map<String,Object> toObject(Point p) {
        Map<String,Object> obj = object();
        obj.put("type", "Point");

        JSONArray l = new JSONArray();
        l.add(p.getX());
        l.add(p.getY());
        if (!Double.isNaN(p.getCoordinate().z)) {
            l.add(p.getCoordinate().z);
        }
        
        obj.put("coordinates", l);
        return obj;
    }

    /**
     * Creates a linestring GeoJSON object.
     * 
     * @param l The linestring.
     * 
     * @return A GeoJSON object as defined by {@linkplain http://www.geojson.org/geojson-spec.html#linestring}
     */
    public static Map<String,Object> toObject(LineString l) {
        Map<String,Object> obj = object();
        obj.put("type", "LineString");
        obj.put("coordinates", toArray(l.getCoordinateSequence()));
        return obj;
    }

    /**
     * Creates a polygon GeoJSON object.
     * 
     * @param p The polygon.
     * 
     * @return A GeoJSON object as defined by {@linkplain http://www.geojson.org/geojson-spec.html#polygon}
     */
    public static Map<String,Object> toObject(Polygon p) {
        Map<String,Object> obj = object();
        obj.put("type", "Polygon");
        obj.put("coordinates", toArray(p)); 
        return obj;
    }

    /**
     * Creates a multipoint GeoJSON object.
     * 
     * @param mp The multipoint.
     * 
     * @return A GeoJSON object as defined by {@linkplain http://www.geojson.org/geojson-spec.html#multipoint}
     */
    public static Map<String,Object> toObject(MultiPoint mp) {
        Map<String,Object> obj = object();
        obj.put("type", "MultiPoint");
        obj.put("coordinates", toArray(mp.getCoordinates()));
        return obj;
    }

    /**
     * Creates a multilinestring GeoJSON object.
     * 
     * @param ml The multilinestring.
     * 
     * @return A GeoJSON object as defined by {@linkplain http://www.geojson.org/geojson-spec.html#multilinestring}
     */
    public static Map<String,Object> toObject(MultiLineString ml) {
        Map<String,Object> obj = object();
        obj.put("type", "MultiLineString");
        obj.put("coordinates", toArray(ml));
        return obj;
    }

    /**
     * Creates a multipolygon GeoJSON object.
     * 
     * @param mp The multipolygon.
     * 
     * @return A GeoJSON object as defined by {@linkplain http://www.geojson.org/geojson-spec.html#multipolygon}
     */
    public static Map<String,Object> toObject(MultiPolygon mp) {
        Map<String,Object> obj = object();
        obj.put("type", "MultiPolygon");
        obj.put("coordinates", toArray(mp));
        return obj;
    }

    /**
     * Creates a geometrycollection GeoJSON object.
     * 
     * @param gc The geometrycollection.
     * 
     * @return A GeoJSON object as defined by {@linkplain http://www.geojson.org/geojson-spec.html#geometry-collection}
     */
    public static Map<String,Object> toObject(GeometryCollection gc) {
        Map<String,Object> obj = object();
        obj.put("type", "GeometryCollection");
        
        JSONArray l = new JSONArray();
        for (Geometry g : Geom.iterate(gc)) {
            l.add(toObject(g));
        }
        obj.put("geometries", l);
        return obj;
    }

    /**
     * Creates a geometry GeoJSON object.
     * 
     * @param g The geometry.
     * 
     * @return A GeoJSON object as defined by {@linkplain http://www.geojson.org/geojson-spec.html#geometry-objects}
     */
    public static Map<String,Object> toObject(Geometry g) {
        if (g == null) {
            return null;
        }

        switch(Geom.Type.from(g)) {
        case POINT:
            return toObject((Point)g);
        case LINESTRING:
            return toObject((LineString)g);
        case POLYGON:
            return toObject((Polygon)g);
        case MULTIPOINT:
            return toObject((MultiPoint)g);
        case MULTILINESTRING:
            return toObject((MultiLineString)g);
        case MULTIPOLYGON:
            return toObject((MultiPolygon)g);
        case GEOMETRYCOLLECTION:
            return toObject((GeometryCollection)g);
        }
        throw new IllegalArgumentException("Unable to encode " + g + " as GeoJSON");
    }

    /**
     * Creates a feature GeoJSON object.
     * 
     * @param f The feature.
     * 
     * @return A GeoJSON object as defined by {@linkplain http://www.geojson.org/geojson-spec.html#feature-objects}
     */
    public static Map<String,Object> toObject(Feature f) {
        if (f == null) {
            return null;
        }

        Map<String,Object> obj = object();

        obj.put("type", "Feature");

        if (f.getId() != null) {
            obj.put("id", f.getId());
        }

        Geometry g = f.geometry();
        if (g != null) {
            obj.put("geometry", toObject(g));
        }

        Map<String,Object> map = new LinkedHashMap<String, Object>(f.map());
        map.remove("geometry");

        obj.put("properties", map);
        return obj;
    }

    Writer out;

    Deque<EncodeState> stack = new ArrayDeque<EncodeState>();
    
    /**
     * Creates a new writer.
     * 
     * @param out The writer to encode to.
     */
    public GeoJSONWriter(Writer out) {
        this.out = out;
    }

    /**
     * The underlying writer.
     */
    public Writer getWriter() {
        return out;
    }

    /**
     * Starts a new JSON object.
     */
    public GeoJSONWriter obj() throws IOException {
        push(new EncodeState());
        out.write("{");
        return this;
    }

    /**
     * Ends the current JSON object.
     */
    public GeoJSONWriter endObj() throws IOException {
        pop();
        out.write("}");
        return this;
    }

    /**
     * Creates a key within the current JSON object.
     * <p>
     * {@link #obj()} must be called prior to calling this method.
     * </p>
     */
    public GeoJSONWriter key(String key) throws IOException {
        EncodeState state = peek();
        if (!state.first) {
            out.write(",");
        }

        state.key = true;

        out.write("\"" + JSONValue.escape(key) + "\"");
        out.write(":");
        return this;
    }

    /**
     * Creates a key within the current JSON object.
     * <p>
     * {@link #key(String)} must be called prior to calling this method.
     * </p>
     */
    public GeoJSONWriter value(Object val) throws IOException {
        write(val);
        return this;
    }

    /**
     * Starts a new array.
     * <p>
     * If the array is not the top level object then {@link #key(String)} must be called 
     * prior to calling this method.
     * </p> 
     */
    public GeoJSONWriter array() throws IOException {
        if (!stack.isEmpty() && !peek().key) {
            throw new IllegalStateException("no key");
        }
        push(new EncodeState(true));
        out.write("[");
        return this;
    }

    /**
     * Ends the current array.
     */
    public GeoJSONWriter endArray() throws IOException {
        pop();
        out.write("]");
        return this;
    }

    /**
     * Encodes a bounding box as a 4 element array.
     * 
     * @param b The The bounding box.
     * @see {@linkplain http://www.geojson.org/geojson-spec.html#bounding-boxes}
     */
    public GeoJSONWriter bbox(Envelope b) throws IOException {
        return write(toObject(b));
    }

    /**
     * Encodes a crs as a named crs.
     * 
     * @param crs The crs object.
     * @see {@linkplain http://www.geojson.org/geojson-spec.html#named-crs}
     */
    public GeoJSONWriter crs(CoordinateReferenceSystem crs)  throws IOException {
        return write(toObject(crs));
    }

    /**
     * Encodes a feature object.
     * 
     * @param f The feature.
     * 
     * @see {@linkplain http://www.geojson.org/geojson-spec.html#feature-objects}
     */
    public GeoJSONWriter feature(Feature f)  throws IOException {
        return write(toObject(f));
    }

    /**
     * Encoded a feature collection.
     *
     * 
     * @see {@linkplain http://www.geojson.org/geojson-spec.html#feature-collection-objects}
     */
    public GeoJSONWriter featureCollection(Cursor<Feature> features)  throws IOException {
        Map<String,Object> obj = object();
        obj.put("type", "FeatureCollection");
        obj.put("features", new FeatureCollection(features));
        return write(obj);
    }

    //
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
        return write(toObject(g));
    }

    /**
     * Encodes a point object.
     * 
     * @param p The point.
     * 
     * @see {@linkplain http://www.geojson.org/geojson-spec.html#point}
     */
    public GeoJSONWriter point(Point p) throws IOException {
        return write(toObject(p));
    }

    /**
     * Encodes a linestring object.
     * 
     * @param l The linestring.
     * 
     * @see {@linkplain http://www.geojson.org/geojson-spec.html#linestring}
     */
    public GeoJSONWriter lineString(LineString l) throws IOException {
        return write(toObject(l));
    }

    /**
     * Encodes a polygon object.
     * 
     * @param p The polygon
     * 
     * @see {@linkplain http://www.geojson.org/geojson-spec.html#polygon}
     */
    public GeoJSONWriter polygon(Polygon p) throws IOException {
        return write(toObject(p));
    }
    
    /**
     * Encodes a multipoint object.
     * 
     * @param mp The multipoint
     * 
     * @see {@linkplain http://www.geojson.org/geojson-spec.html#multipoint}
     */
    public GeoJSONWriter multiPoint(MultiPoint mp) throws IOException {
        return write(toObject(mp));
    }

    /**
     * Encodes a multilinestring object.
     * 
     * @param ml The multilinestring
     * 
     * @see {@linkplain http://www.geojson.org/geojson-spec.html#multilinestring}
     */
    public GeoJSONWriter multiLineString(MultiLineString ml) throws IOException {
        return write(toObject(ml));
    }

    /**
     * Encodes a multipolygon object.
     * 
     * @param mp The multipolygon
     * 
     * @see {@linkplain http://www.geojson.org/geojson-spec.html#multipolygon}
     */
    public GeoJSONWriter multiPolygon(MultiPolygon mp) throws IOException {
        return write(toObject(mp));
    }

    /**
     * Encodes a geometry collection object.
     * 
     * @param gc The geometry collection.
     * 
     * @see {@linkplain http://www.geojson.org/geojson-spec.html#geometry-collection}
     */
    public GeoJSONWriter geometryCollection(GeometryCollection gc) throws IOException {
        return write(toObject(gc));
    }

    GeoJSONWriter write(Object obj) throws IOException {
        if (stack.isEmpty()) {
            //single object case
            JSONValue.writeJSONString(obj, out);

            //place sentinel on stack to signal we are done
            stack.push(DONE);
        }
        else {
            EncodeState state = peek();
            if (state.array) {
                if (!state.first) {
                    out.write(",");
                }
                JSONValue.writeJSONString(obj, out);
            }
            else {
                if (!state.key) {
                    throw new IllegalStateException("Object has no key");
                }

                JSONValue.writeJSONString(obj, out);
            }

            state.first = false;
        }
        return this;
    }

    EncodeState peek() {
        if (stack.isEmpty() || stack.peek() == DONE) {
            throw new IllegalStateException("No object");
        }
        return stack.peek();
    }

    EncodeState pop() {
        if (stack.isEmpty() || stack.peek() == DONE) {
            throw new IllegalStateException("No object");
        }
        return stack.pop();
    }

    void push(EncodeState state) {
        if (!stack.isEmpty() && stack.peek() == DONE) {
            throw new IllegalStateException("Unexpected object");
        }
        stack.push(state);
    }

    static Map<String,Object> object() {
        return new LinkedHashMap<String, Object>();
    }

    static JSONArray toArray(Coordinate[] coords) {
        return toArray(new CoordinateArraySequence(coords));
    }

    static JSONArray toArray(CoordinateSequence coordseq) {
        JSONArray l = new JSONArray();
        int dim = coordseq.getDimension();
        for (int i = 0; i < coordseq.size(); i++) {
            JSONArray m = new JSONArray();
            m.add(coordseq.getOrdinate(i, 0));
            m.add(coordseq.getOrdinate(i, 1));
            if (dim > 2) {
                double v = coordseq.getOrdinate(i, 2);
                if (!Double.isNaN(v)) {
                    m.add(v);
                }
            }
            l.add(m);
        }
        return l;
    }

    static JSONArray toArray(Polygon p) {
        JSONArray l = new JSONArray();
        l.add(toArray(p.getExteriorRing().getCoordinateSequence()));
        for (int i = 0; i < p.getNumInteriorRing(); i++) {
            l.add(toArray(p.getInteriorRingN(i).getCoordinateSequence()));
        }
        return l;
    }

    static JSONArray toArray(MultiLineString ml) {
        JSONArray l = new JSONArray();
        for (LineString ls : Geom.iterate(ml)) {
            l.add(toArray(ls.getCoordinateSequence()));
        }
        return l;
    }

    static JSONArray toArray(MultiPolygon mp) {
        JSONArray l = new JSONArray();
        for (Polygon p : Geom.iterate(mp)) {
            l.add(toArray(p));
        }
        return l;
    }

    static EncodeState DONE = new EncodeState();

    static class EncodeState {
        boolean first;
        boolean array;
        boolean key;
        
        EncodeState() {
            this(false);
        }

        EncodeState(boolean array) {
            this.array = array;
            this.first = true;
        }
    }

    class FeatureCollection implements JSONStreamAware {
        Cursor<Feature> cursor;

        FeatureCollection(Cursor<Feature> cursor) {
            this.cursor = cursor;
        }

        @Override
        public void writeJSONString(Writer out) throws IOException {
            out.write("[");

            for (Feature f : cursor) {
                out.write(JSONValue.toJSONString(toObject(f)));
            }

            out.write("]");
            out.flush();
        }
   }
}
