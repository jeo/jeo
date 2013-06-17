package org.jeo.sql;

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

/**
 * Contains mappings between database types and java types.
 *  
 * @author Justin Deoliveira, OpenGeo
 */
public class DbTypes {

    /**
     * sql type to class mappings.
     */
    Map<Integer,Class<?>> sql;

    /**
     * name to class mappings
     */
    Map<String,Class<?>> name;

    public DbTypes() {
        sql = createSqlTypeMappings();
        name = createNameMappings();
    }

    /**
     * Creates sql type (defined by {@link Types}) to class mappings.
     * <p>
     * Subclasses should override/extend this method to customize mappings.
     * </p>
     */
    protected Map<Integer, Class<?>> createSqlTypeMappings() {
        Map<Integer, Class<?>> sql = new LinkedHashMap<Integer, Class<?>>();
        sql.put(Types.VARCHAR, String.class);
        sql.put(Types.CHAR, String.class);
        sql.put(Types.LONGVARCHAR, String.class);
        sql.put(Types.NVARCHAR, String.class);
        sql.put(Types.NCHAR, String.class);
        
        sql.put(Types.BIT, Boolean.class);
        sql.put(Types.BOOLEAN, Boolean.class);

        sql.put(Types.TINYINT, Short.class);
        sql.put(Types.SMALLINT, Short.class);

        sql.put(Types.INTEGER, Integer.class);
        sql.put(Types.BIGINT, Long.class);

        sql.put(Types.REAL, Float.class);
        sql.put(Types.DOUBLE, Double.class);
        sql.put(Types.FLOAT, Double.class);

        sql.put(Types.DECIMAL, BigDecimal.class);
        sql.put(Types.NUMERIC, BigDecimal.class);

        sql.put(Types.DATE, Date.class);
        sql.put(Types.TIME, Time.class);
        sql.put(Types.TIMESTAMP, Timestamp.class);
        
        sql.put(Types.BLOB, byte[].class);
        sql.put(Types.BINARY, byte[].class);
        sql.put(Types.CLOB, String.class);
        
        sql.put(Types.VARBINARY, byte[].class);
        return sql;
    }

    /**
     * Creates sql type name  to class mappings.
     * <p>
     * Subclasses should override/extend this method to customize mappings.
     * </p>
     */
    protected Map<String, Class<?>> createNameMappings() {
        Map<String,Class<?>> name = new LinkedHashMap<String, Class<?>>();
        name.put("GEOMETRY", Geometry.class);
        name.put("POINT", Point.class);
        name.put("LINESTRING", LineString.class);
        name.put("POLYGON", Polygon.class);
        name.put("POLYGONM", Polygon.class);
        name.put("MULTIPOINT", MultiPoint.class);
        name.put("MULTILINESTRING", MultiLineString.class);
        name.put("MULTIPOLYGON", MultiPolygon.class);
        name.put("GEOMETRYCOLLECTION", GeometryCollection.class);
        return name;
    }

    /**
     * Returns the class mapped to the specified type.
     * 
     * @param type Type defined by {@link Types}.
     * 
     * @return The mapped class or <code>null</code> if no such mapping exists.
     */
    public Class<?> fromSQL(int type) {
        return sql.get(type);
    }

    /**
     * Returns the class mapped to the specified type name.
     * 
     * @param typename The name of the type.
     * 
     * @return The mapped class or <code>null</code> if no such mapping exists.
     */
    public Class<?> fromName(String typename) {
        return name.get(typename.toUpperCase());
    }

    /**
     * Returns the sql type (from {@link Types}) for the specified class.
     * <p>
     * This method will first try a direct match. If no such mapping exists for the exact class
     * then a loose match is done by returning the first mapped class that is assignable from the
     * specified class.
     * </p>
     * @param clazz The mapped class.
     * 
     * @return The sql type or <code>null</code> if no such mapping exists.
     */
    public Integer toSQL(Class<?> clazz) {
        for (Map.Entry<Integer,Class<?>> e : sql.entrySet()) {
            if (e.getValue() == clazz) {
                return e.getKey();
            }
        }

        //no match, do a loose match
        for (Map.Entry<Integer,Class<?>> e : sql.entrySet()) {
            if (e.getValue().isAssignableFrom(clazz)) {
                return e.getKey();
            }
        }

        return null;
    }

    /**
     * Returns the sql type name for the specified class.
     * <p>
     * This method will first try a direct match. If no such mapping exists for the exact class
     * then a loose match is done by returning the first mapped class that is assignable from the
     * specified class.
     * </p>
     * @param clazz The mapped class.
     * 
     * @return The sql type name or <code>null</code> if no such mapping exists.
     */
    public String toName(Class<?> clazz) {
        for (Map.Entry<String,Class<?>> e : name.entrySet()) {
            if (e.getValue() == clazz) {
                return e.getKey();
            }
        }

        //no match, do a loose match
        for (Map.Entry<String,Class<?>> e : name.entrySet()) {
            if (e.getValue().isAssignableFrom(clazz)) {
                return e.getKey();
            }
        }

        return null;
    }
}
