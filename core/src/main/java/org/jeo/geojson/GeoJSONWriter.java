package org.jeo.geojson;

import org.jeo.feature.Feature;
import org.jeo.feature.Field;
import org.jeo.feature.Schema;
import org.jeo.geom.Geometries;
import org.json.JSONException;
import org.json.JSONStringer;

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

public class GeoJSONWriter extends JSONStringer {


    public GeoJSONWriter bbox(Envelope b) {
        try {
            key("bbox").array()
                .value(b.getMinX()).value(b.getMinY()).value(b.getMaxX()).value(b.getMaxY());
            endArray();
        }
        catch(JSONException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public GeoJSONWriter crs(int epsg) {
        try {
            object();
            key("type").value("name");
            key("properties").object();
            value("EPSG:"+epsg);
            endObject();
            endObject();
        }
        catch(JSONException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public GeoJSONWriter feature(Feature f) {
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
            throw new RuntimeException(e);
        }
        return this;
    }

    public GeoJSONWriter featureCollection() {
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

    public GeoJSONWriter endFeatureCollection() {
        try {
            endArray();
            return (GeoJSONWriter) endObject();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    //
    // geometry methods
    //
    
    public GeoJSONWriter geometry(Geometry g) {
        if (g == null) {
            try {
                return (GeoJSONWriter) this.value(null);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        switch(Geometries.get(g)) {
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

    public GeoJSONWriter point(Point p) {
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
            throw new RuntimeException(e);
        }

        return this;
    }

    public GeoJSONWriter lineString(LineString l) {
        try {
            object();
            key("type").value("LineString");
            key("coordinates");
            coordinates(l.getCoordinateSequence());
            endObject();
        }
        catch(JSONException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public GeoJSONWriter polygon(Polygon p) {
        try {
            object();
            key("type").value("Polygon");
            key("coordinates");
            
            coordinates(p);
            endObject();
        }
        catch(JSONException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public GeoJSONWriter multiPoint(MultiPoint mp) {
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
            throw new RuntimeException(e);
        }
        return this;
    }

    public GeoJSONWriter multiLineString(MultiLineString ml) {
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
            throw new RuntimeException(e);
        }
        return this;
    }

    public GeoJSONWriter multiPolygon(MultiPolygon mp) {
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
            throw new RuntimeException(e);
        }
        return this;
    }

    public GeoJSONWriter geometryCollection(GeometryCollection gc) {
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
            throw new RuntimeException(e);
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
