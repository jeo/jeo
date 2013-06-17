package org.jeo.postgis;

import java.util.Map;

import org.jeo.sql.DbTypes;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;

public class PostGISTypes extends DbTypes {

    @Override
    protected Map<String, Class<?>> createNameMappings() {
        Map<String, Class<?>> name = super.createNameMappings();
        name.put("GEOGRAPHY", Geometry.class);
        name.put("POINTM", Point.class);
        name.put("LINESTRINGM", LineString.class);
        name.put("MULTIPOINTM", MultiPoint.class);
        name.put("MULTILINESTRINGM", MultiLineString.class);
        name.put("MULTIPOLYGONM", MultiPolygon.class);
        name.put("GEOMETRYCOLLECTIONM", GeometryCollection.class);
        name.put("BYTEA", byte[].class);
        return name;
    }
}
