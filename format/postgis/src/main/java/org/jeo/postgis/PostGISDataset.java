package org.jeo.postgis;

import static org.jeo.postgis.PostGISWorkspace.LOG;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jeo.data.Cursor;
import org.jeo.data.Driver;
import org.jeo.data.Query;
import org.jeo.data.VectorData;
import org.jeo.feature.Feature;
import org.jeo.feature.Field;
import org.jeo.feature.Schema;
import org.jeo.geom.Envelopes;
import org.jeo.sql.DbOP;
import org.jeo.sql.PrimaryKey;
import org.jeo.sql.PrimaryKeyColumn;
import org.jeo.sql.SQL;
import org.jeo.sql.Table;
import org.jeo.util.Pair;
import org.jeo.util.Util;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKTWriter;

public class PostGISDataset implements VectorData {

    Table table;
    PostGISWorkspace pg;

    PostGISDataset(Table table, PostGISWorkspace pg) {
        this.table = table;
        this.pg = pg;
    }

    public Table getTable() {
        return table;
    }

    @Override
    public Driver<?> getDriver() {
        return pg.getDriver();
    }

    @Override
    public String getName() {
        return table.getName();
    }

    @Override
    public String getTitle() {
        return getName();
    }

    @Override
    public String getDescription() {
        return getName();
    }

    @Override
    public Schema getSchema() {
        return table.getSchema();
    }

    @Override
    public CoordinateReferenceSystem getCRS() {
        return getSchema().crs();
    }

    @Override
    public Envelope bounds() throws IOException {
        if (getSchema().geometry() == null) {
            return null;
        }

        return pg.run(new DbOP<Envelope>() {
            @Override
            protected Envelope doRun(Connection cx) throws Exception {
                Schema schema = getSchema();

                String sql = new SQL("SELECT st_asbinary(st_force_2d(st_extent(")
                    .name(schema.geometry().getName()).add(")))")
                    .add(" FROM ").name(schema.getName()).toString();
                LOG.debug(sql);

                ResultSet rs = open(open(cx.createStatement()).executeQuery(sql));
                rs.next();

                byte[] wkb = rs.getBytes(1);
                return new WKBReader().read(wkb).getEnvelopeInternal();
            }
        });
    }

    @Override
    public long count(final Query q) throws IOException {
        return pg.run(new DbOP<Long>() {
            @Override
            protected Long doRun(Connection cx) throws Exception {
                SQL sql = new SQL("SELECT count(*) FROM ").name(getSchema().getName());
                List<Pair<Object,Integer>> values = encodeQueryPredicate(sql, q);

                pg.logQuery(sql, values);

                PreparedStatement ps = open(pg.prepareStatement(sql, values, cx));
                
                ResultSet rs = open(ps.executeQuery());
                rs.next();
                return rs.getLong(1);
            }
        });
    }

    @Override
    public Cursor<Feature> cursor(Query q) throws IOException {
        try {
            Connection cx = pg.getDataSource().getConnection();
            
            if (q.getMode() == Cursor.APPEND) {
                return new PostGISAppendCursor(this, cx);
            }
    
            Schema schema = getSchema();
    
            SQL sql = new SQL("SELECT ");
            
            if (q.getFields().isEmpty()) {
                //grab all from the schema
                for (Field f : getSchema()) {
                    encodeFieldForSelect(f, sql);
                    sql.add(", ");
                }
                sql.trim(2);
            }
            else {
                // use specified, but ensure geometry included
                // TODO: be smarter about this, only include geometry if we have a filter that requires
                // it, etc...
                boolean geom = false;
                for (String prop : q.getFields()) {
                    Field f = getSchema().field(prop);
                    if (f == null) {
                        throw new IllegalArgumentException("No such field: " + prop);
                    }
    
                    encodeFieldForSelect(f, sql);
                    sql.add(", ");
    
                    geom = geom || f.isGeometry();
                }
                sql.trim(2);
    
                
                if (!geom && schema.geometry() != null) {
                    encodeFieldForSelect(schema.geometry(), sql.add(", "));
                }
            }
    
            sql.add(" FROM ").name(schema.getName());
            
            List<Pair<Object,Integer>> values = encodeQueryPredicate(sql, q);
            pg.logQuery(sql, values);

            try {
                PreparedStatement st = pg.prepareStatement(sql, values, cx);
                return q.apply(new PostGISCursor(st.executeQuery(), cx, q.getMode(), this));
            }
            catch(SQLException e) {
                cx.close();
                throw e;
            }
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void close() {
    }

    void encodeFieldForSelect(Field f, SQL sql) {
        if (f.isGeometry()) {
            //TODO: force 2d
            //TODO: base64 encode
            sql.add("ST_AsBinary(").name(f.getName()).add(") as ").name(f.getName());
        }
        else {
            sql.name(f.getName());
        }
    }

    List<Pair<Object,Integer>> encodeQueryPredicate(SQL sql, Query q) {
        Schema schema = getSchema();

        List<Pair<Object,Integer>> values = new ArrayList<Pair<Object,Integer>>();
        
        if (schema.geometry() != null && q.getBounds() != null && !q.getBounds().isNull()) {
            String geom = schema.geometry().getName();
            Integer srid = schema.geometry().property("srid", Integer.class);
            
            Polygon poly = Envelopes.toPolygon(q.getBounds());

            sql.add(" WHERE ").name(geom).add(" && ST_GeomFromText(?, ?)")
               .add(" AND ST_Intersects(").name(geom).add(", ST_GeomFromText(?, ?))");

            String wkt = poly.toText();
            values.add(new Pair(wkt, Types.VARCHAR));
            values.add(new Pair(srid, Types.INTEGER));
            values.add(new Pair(wkt, Types.VARCHAR));
            values.add(new Pair(srid ,Types.INTEGER));
        }

        Integer offset = q.consume(Query.OFFSET, null);
        if (offset != null) {
            sql.add(" OFFSET ").add(offset);
            //values.add(new Pair(offset, Types.INTEGER));
        }
        Integer limit = q.consume(Query.LIMIT, null);
        if (limit != null) {
            sql.add(" LIMIT ").add(limit);
            //values.add(new Pair(limit, Types.INTEGER));
        }

        return values;
    }

    

    void doUpdate(final Feature f, final Map<String,Object> changed, Connection cx) throws IOException {
        pg.run(new DbOP<Boolean>() {
            @Override
            protected Boolean doRun(Connection cx) throws Exception {
                Schema schema = getSchema();
                List<Pair<Object,Integer>> values = new ArrayList<Pair<Object,Integer>>();

                SQL sql = new SQL("UPDATE ").name(schema.getName()).add(" SET ");
                for (String col : changed.keySet()) {
                    sql.name(col).add(" = ?,");

                    Field fld = schema.field(col);
                    values.add(new Pair(f.get(col),  (Integer) fld.property("sqlType", Integer.class)));
                }
                sql.trim(1);
                sql.add(" WHERE ");

                List<PrimaryKeyColumn> pkcols = getTable().getPrimaryKey().getColumns(); 
                for (PrimaryKeyColumn pkcol : pkcols) {
                    String col = pkcol.getName();
                    sql.name(col).add(" = ?,");

                    Field fld = schema.field(col);
                    values.add(new Pair(f.get(col), fld.property("sqlType", Integer.class)));
                }
                sql.trim(1);

                pg.logQuery(sql, values);

                PreparedStatement ps = open(cx.prepareStatement(sql.toString()));
                for (int i = 0; i < values.size(); i++) {
                    Pair<Object,Integer> p = values.get(i);
                    ps.setObject(i+1, p.first(), p.second());
                }

                return ps.execute();
            }
        }, cx);
    }
    
    void doInsert(final Feature f, Connection cx) throws IOException {
        pg.run(new DbOP<Boolean>() {
            @Override
            protected Boolean doRun(Connection cx) throws Exception {
                Schema schema = getSchema();
                List<Pair<Object,Integer>> values = new ArrayList<Pair<Object,Integer>>();

                PrimaryKey pkey = getTable().getPrimaryKey();

                SQL cols = new SQL("INSERT INTO ").name(schema.getName()).add(" (");
                SQL vals = new SQL("VALUES (");

                for (Field fld : schema) {
                    PrimaryKeyColumn pkcol = pkey.column(fld.getName());
                    Object value = null;
                    if (pkcol != null) {
                        if (pkcol.isAutoIncrement()) {
                            continue;
                        }

                        if (pkcol.getSequence() != null) {
                            throw new IllegalArgumentException("TODO: implement");
                        }
                        else {
                            //generate one
                            value = nextval(pkcol, fld.getType(), cx);
                        }
                    }
                    else {
                        value = f.get(fld.getName());
                    }

                    cols.name(fld.getName()).add(",");

                    if (value instanceof Geometry) {
                        value = new WKTWriter().write((Geometry) value);
                        values.add(new Pair(value, Types.VARCHAR));

                        Integer srid = fld.property("srid", Integer.class);
                        srid = srid != null ? srid : -1;
                        vals.add("st_geomfromtext(?,").add(srid).add("),");
                    }
                    else {
                        Integer sqlType = fld.property("sqlType", Integer.class);
                        values.add(new Pair(value, sqlType));
                        vals.add("?,");
                    }
                }
                vals.trim(1).add(")");
                cols.trim(1).add(") ").add(vals.toString());

                pg.logQuery(cols, values);

                PreparedStatement ps = cx.prepareStatement(cols.toString());
                for (int i = 0; i < values.size(); i++) {
                    Pair<Object,Integer> p = values.get(i);
                    Object obj = p.first();
                    if (obj == null) {
                        ps.setNull(i+1, p.second());
                    }
                    else {
                        //ps.setNull(i+1, p.second());
                        ps.setObject(i+1, obj, p.second());
                    }
                }

                return ps.executeUpdate() > 0;
            }
        }, cx);
    }

    <T> T nextval(final PrimaryKeyColumn pkcol, Class<T> type, Connection cx) throws IOException {
        if (CharSequence.class.isAssignableFrom(type)) {
            return type.cast(Util.uuid());
        }
        else if (Number.class.isAssignableFrom(type) && type == Long.class ||
            type == Integer.class || type == Short.class || type == Byte.class ||  
            BigInteger.class.isAssignableFrom(type) || BigDecimal.class.isAssignableFrom(type)) {
            
            return type.cast(pg.run(new DbOP<Number>() {
                @Override
                protected Number doRun(Connection cx) throws Exception {
                    SQL sql = new SQL("SELECT max(").name(pkcol.getName()).add(")+1 FROM ")
                        .name(getSchema().getName());

                    Statement st = open(cx.createStatement());
                    ResultSet rs = open(st.executeQuery(sql.toString()));
                    if (rs.next()) {
                        return (Number) rs.getObject(1);
                    }

                    return 1;
                }
            }, cx));
        }
        else {
            throw new IllegalArgumentException(String.format(
                "Unable to generate value for %s.%s", getSchema().getName(), pkcol.getName()));
        }
    }

    
}
