package org.jeo.geojson;

import java.io.IOException;

import org.jeo.data.Cursor;
import org.jeo.feature.Feature;
import org.jeo.feature.Field;
import org.jeo.feature.Schema;
import org.jeo.geom.Geom;
import org.jeo.proj.Proj;
import org.json.JSONException;
import org.json.JSONStringer;
import org.osgeo.proj4j.CoordinateReferenceSystem;

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

/**
 * Encodes objects as GeoJSON.
 * <p>
 * Example usage:
 * <pre>
 * GeoJSONWriter w = new GeoJSONWriter();
 * w.object();
 * w.key("myobj").geometry(...);
 * e.endObject();
 * 
 * String json = w.toString();
 * </pre>
 * </p>
 * @see {@linkplain http://geojson.org}
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class GeoJSONWriter extends JSONStringer {

    /**
     * Encodes an object as GeoJSON.
     * <p>
     * This method can handle the following types of objects.
     * <ul>
     * <li>All types of {@link Geometry}
     * <li>{@link Feature}
     * <li>{@link Cursor} of {@link Feature}
     * <li>{@link Envelope}
     * <li>{@link CoordinateReferenceSystem}
     * </ul>
     * </p>
     * @param o Object to encode.
     * 
     * @return This object.
     * 
     * @throws IOException In the event of an encoding error.
     * @throws IllegalArgumentException If <tt>obj</tt> is unrecognized.
     */
    @SuppressWarnings("unchecked")
    public GeoJSONWriter write(Object o) throws IOException {
        if (o instanceof Geometry) {
            geometry((Geometry)o);
        }
        else if (o instanceof Feature) {
            feature((Feature)o);
        }
        else if (o instanceof Cursor) {
            featureCollection();
            for (Feature f : (Cursor<Feature>)o) {
                feature(f);
            }
            endFeatureCollection();
        }
        else if (o instanceof CoordinateReferenceSystem) {
            crs((CoordinateReferenceSystem)o);
        }
        else if (o instanceof Envelope){
            bbox((Envelope)o);
        }
        else {
            throw new IllegalArgumentException("Unable to encode " + o);
        }

        return this;
    }

    /**
     * Encodes a bounding box as a 4 element array.
     * 
     * @param b The The bounding box.
     * @see {@linkplain http://www.geojson.org/geojson-spec.html#bounding-boxes}
     */
    public GeoJSONWriter bbox(Envelope b) throws IOException {
        try {
            array()
                .value(b.getMinX()).value(b.getMinY()).value(b.getMaxX()).value(b.getMaxY());
            endArray();
        }
        catch(JSONException e) {
            throw new IOException(e);
        }
        return this;
    }

    /**
     * Encodes a crs as a named crs.
     * 
     * @param crs The crs object.
     * @see {@linkplain http://www.geojson.org/geojson-spec.html#named-crs}
     */
    public GeoJSONWriter crs(CoordinateReferenceSystem crs)  throws IOException {
        return crs(Proj.epsgCode(crs));
    }

    /**
     * Encodes an epsg code as a named crs.
     * 
     * @param epsg The epsg code.
     * 
     * @see {@linkplain http://www.geojson.org/geojson-spec.html#named-crs}
     */
    public GeoJSONWriter crs(int epsg) throws IOException{
        try {
            object();
            key("type").value("name");
            key("properties").object();
            value("EPSG:"+epsg);
            endObject();
            endObject();
        }
        catch(JSONException e) {
            throw new IOException(e);
        }
        return this;
    }

    /**
     * Encodes a feature object.
     * 
     * @param f The feature.
     * 
     * @see {@linkplain http://www.geojson.org/geojson-spec.html#feature-objects}
     */
    public GeoJSONWriter feature(Feature f)  throws IOException {
        try {
            object();

            key("type").value("Feature");
            key("geometry");
            geometry(f.geometry());

            key("properties").object();

            Schema schema = f.schema();
            Field geometry = schema.geometry();
            for (Field fld : schema) {
                if (!fld.equals(geometry)) {
                    key(fld.getName()).value(f.get(fld.getName()));
                }
            }

            endObject();

            endObject();
        }
        catch(JSONException e) {
            throw new IOException(e);
        }
        return this;
    }

    /**
     * Starts a feature collection.
     * <p>
     * This method is intended to be used in conjunction with the {@link #feature(Feature)} 
     * method. For example: 
     * <pre>
     * featureCollection();
     * feature(f1);
     * feature(f2);
     * ...
     * endFeatureCollection();
     * </pre>
     * </p>
     * 
     * @see {@linkplain http://www.geojson.org/geojson-spec.html#feature-collection-objects}
     */
    public GeoJSONWriter featureCollection()  throws IOException {
        try {
            object();
            key("type").value("FeatureCollection");
            key("features").array();
        }
        catch(JSONException e) {
            throw new RuntimeException(e);
        }

        return this;
    }

    /**
     * Ends a feature collection
     * <p>
     * This method must be called after {@link #featureCollection()}
     * </p>
     */
    public GeoJSONWriter endFeatureCollection()  throws IOException {
        try {
            endArray();
            return (GeoJSONWriter) endObject();
        } catch (JSONException e) {
            throw new IOException(e);
        }
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
        if (g == null) {
            try {
                return (GeoJSONWriter) this.value(null);
            } catch (JSONException e) {
                throw new IOException(e);
            }
        }

        switch(Geom.Type.fromObject(g)) {
        case POINT:
            return point((Point)g);
        case LINESTRING:
            return lineString((LineString)g);
        case POLYGON:
            return polygon((Polygon)g);
        case MULTIPOINT:
            return multiPoint((MultiPoint)g);
        case MULTILINESTRING:
            return multiLineString((MultiLineString)g);
        case MULTIPOLYGON:
            return multiPolygon((MultiPolygon)g);
        case GEOMETRYCOLLECTION:
            return geometryCollection((GeometryCollection)g);
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
        try {
            object();
            key("type").value("Point");
            key("coordinates").array().value(p.getX()).value(p.getY());
            if (!Double.isNaN(p.getCoordinate().z)) {
                value(p.getCoordinate().z);
            }

            endArray();
            endObject();
        }
        catch(JSONException e) {
            throw new IOException(e);
        }

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
        try {
            object();
            key("type").value("LineString");
            key("coordinates");
            coordinates(l.getCoordinateSequence());
            endObject();
        }
        catch(JSONException e) {
            throw new IOException(e);
        }
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
        try {
            object();
            key("type").value("Polygon");
            key("coordinates");
            
            coordinates(p);
            endObject();
        }
        catch(JSONException e) {
            throw new IOException(e);
        }
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
        try {
            object();
            key("type").value("MultiPoint");
            key("coordinates");
            array();

            for (int i = 0; i < mp.getNumGeometries(); i++) {
                Point p = (Point) mp.getGeometryN(i);
                array().value(p.getX()).value(p.getY()).endArray();
            }
            
            endArray();
            endObject();
        }
        catch(JSONException e) {
            throw new IOException(e);
        }
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
        try {
            object();
            key("type").value("MultiLineString");
            key("coordinates");
            array();

            for (int i = 0; i < ml.getNumGeometries(); i++) {
                LineString l = (LineString) ml.getGeometryN(i);
                coordinates(l.getCoordinateSequence());
            }

            endArray();
            endObject();
        }
        catch(JSONException e) {
            throw new IOException(e);
        }
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
        try {
            object();
            key("type").value("MultiPolygon");
            key("coordinates");
            array();

            for (int i = 0; i < mp.getNumGeometries(); i++) {
                Polygon p = (Polygon) mp.getGeometryN(i);
                coordinates(p);
            }

            endArray();
            endObject();
        }
        catch(JSONException e) {
            throw new IOException(e);
        }
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
        try {
            object();
            key("type").value("GeometryCollection");
            key("geometries");
            array();

            for (int i = 0; i < gc.getNumGeometries(); i++) {
                geometry(gc.getGeometryN(i));
            }

            endArray();
            endObject();
        }
        catch(JSONException e) {
            throw new IOException(e);
        }
        return this;
    }

    JSONStringer coordinates(CoordinateSequence coords) throws JSONException {
        array();
        for (int i = 0; i < coords.size(); i++) {
            array().value(coords.getX(i)).value(coords.getY(i)).endArray();
        }
        endArray();
        return this;
    }

    JSONStringer coordinates(Polygon p) throws JSONException {
        array();
        coordinates(p.getExteriorRing().getCoordinateSequence());
        for (int i = 0; i < p.getNumInteriorRing(); i++) {
            coordinates(p.getInteriorRingN(i).getCoordinateSequence());
        }
        endArray();
        return this;
    }

}
