package org.jeo.postgis;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.LinkedHashMap;
import java.util.Map;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class TypeMappings {

    static Map<Integer,Class<?>> SQL = new LinkedHashMap<Integer, Class<?>>();
    static {
        SQL.put(Types.VARCHAR, String.class);
        SQL.put(Types.CHAR, String.class);
        SQL.put(Types.LONGVARCHAR, String.class);
        SQL.put(Types.NVARCHAR, String.class);
        SQL.put(Types.NCHAR, String.class);
        
        SQL.put(Types.BIT, Boolean.class);
        SQL.put(Types.BOOLEAN, Boolean.class);

        SQL.put(Types.TINYINT, Short.class);
        SQL.put(Types.SMALLINT, Short.class);

        SQL.put(Types.INTEGER, Integer.class);
        SQL.put(Types.BIGINT, Long.class);

        SQL.put(Types.REAL, Float.class);
        SQL.put(Types.FLOAT, Double.class);
        SQL.put(Types.DOUBLE, Double.class);

        SQL.put(Types.DECIMAL, BigDecimal.class);
        SQL.put(Types.NUMERIC, BigDecimal.class);

        SQL.put(Types.DATE, Date.class);
        SQL.put(Types.TIME, Time.class);
        SQL.put(Types.TIMESTAMP, Timestamp.class);
        
        SQL.put(Types.BLOB, byte[].class);
        SQL.put(Types.BINARY, byte[].class);
        SQL.put(Types.CLOB, String.class);
        
        SQL.put(Types.VARBINARY, byte[].class);
    }

    static Map<String,Class<?>> NAME = new LinkedHashMap<String, Class<?>>();
    static {
        NAME.put("GEOMETRY", Geometry.class);
        NAME.put("GEOGRAPHY", Geometry.class);
        NAME.put("POINT", Point.class);
        NAME.put("POINTM", Point.class);
        NAME.put("LINESTRING", LineString.class);
        NAME.put("LINESTRINGM", LineString.class);
        NAME.put("POLYGON", Polygon.class);
        NAME.put("POLYGONM", Polygon.class);
        NAME.put("MULTIPOINT", MultiPoint.class);
        NAME.put("MULTIPOINTM", MultiPoint.class);
        NAME.put("MULTILINESTRING", MultiLineString.class);
        NAME.put("MULTILINESTRINGM", MultiLineString.class);
        NAME.put("MULTIPOLYGON", MultiPolygon.class);
        NAME.put("MULTIPOLYGONM", MultiPolygon.class);
        NAME.put("GEOMETRYCOLLECTION", GeometryCollection.class);
        NAME.put("GEOMETRYCOLLECTIONM", GeometryCollection.class);
        NAME.put("BYTEA", byte[].class);
    }

    public static Class<?> fromSQL(int type) {
        return SQL.get(type);
    }

    public static Class<?> fromName(String name) {
        return NAME.get(name.toUpperCase());
    }
}
