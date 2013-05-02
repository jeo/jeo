package org.jeo.postgis;

import static org.jeo.postgis.PostGISWorkspace.LOG;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.jeo.data.Cursor;
import org.jeo.data.Driver;
import org.jeo.data.Query;
import org.jeo.data.VectorData;
import org.jeo.feature.Feature;
import org.jeo.feature.Field;
import org.jeo.feature.Schema;
import org.jeo.geom.Geom;
import org.jeo.util.Pair;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKBReader;

public class PostGISDataset implements VectorData {

    Schema schema;
    PostGISWorkspace pg;

    PostGISDataset(Schema schema, PostGISWorkspace pg) {
        this.schema = schema;
        this.pg = pg;
    }

    @Override
    public Driver<?> getDriver() {
        return pg.getDriver();
    }

    @Override
    public String getName() {
        return schema.getName();
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
    public Schema getSchema() throws IOException {
        return schema;
    }

    @Override
    public CoordinateReferenceSystem getCRS() {
        return schema.crs();
    }

    @Override
    public Envelope bounds() throws IOException {
        if (schema.geometry() == null) {
            return null;
        }

        return pg.run(new DbOP<Envelope>() {
            @Override
            protected Envelope doRun(Connection cx) throws Exception {

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
                SQL sql = new SQL("SELECT count(*) FROM ").name(schema.getName());
                List<Pair<Object,Integer>> values = encodeQueryPredicate(sql, q);

                logQuery(sql, values);

                PreparedStatement ps = open(prepareStatement(sql, values, cx));
                
                ResultSet rs = open(ps.executeQuery());
                rs.next();
                return rs.getLong(1);
            }
        });
    }

    @Override
    public Cursor<Feature> cursor(Query q) throws IOException {
        SQL sql = new SQL("SELECT ");
        
        if (q.getFields().isEmpty()) {
            //grab all from the schema
            for (Field f : schema) {
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
                Field f = schema.field(prop);
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
        logQuery(sql, values);

        try {
            Connection cx = pg.getDataSource().getConnection();
            try {
                PreparedStatement st = cx.prepareStatement(sql.toString());
                return q.apply(new PostGISCursor(st.executeQuery(), this));
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
    public void dispose() {
    }

    void encodeFieldForSelect(Field f, SQL sql) {
        if (f.isGeometry()) {
            //TODO: force 2d
            //TODO: base64 encode
            sql.add("st_asbinary(").name(f.getName()).add(") as ").name(f.getName());
        }
        else {
            sql.name(f.getName());
        }
    }

    List<Pair<Object,Integer>> encodeQueryPredicate(SQL sql, Query q) {
        List<Pair<Object,Integer>> values = new ArrayList<Pair<Object,Integer>>();
        
        if (schema.geometry() != null && q.getBounds() != null) {
            String geom = schema.geometry().getName();
            Integer srid = (Integer) schema.geometry().getProperty("srid");
            
            Polygon poly = Geom.toPolygon(q.getBounds());

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
            values.add(new Pair(offset, Types.INTEGER));
        }
        Integer limit = q.consume(Query.LIMIT, null);
        if (limit != null) {
            sql.add(" LIMIT ").add(limit);
            values.add(new Pair(limit, Types.INTEGER));
        }

        return values;
    }

    void logQuery(SQL sql, List<Pair<Object,Integer>> values) {
        if (LOG.isDebugEnabled()) {
            StringBuilder msg = new StringBuilder(sql.toString()).append("; ");
            for (int i = 0; i < values.size(); i++) {
                msg.append(String.format("%d=%s", i+1, values.get(i).second()))
                   .append(", ");
            }
            msg.setLength(msg.length()-2);
            LOG.debug(msg.toString());
        }
    }

    PreparedStatement prepareStatement(SQL sql, List<Pair<Object,Integer>> values, Connection cx) 
            throws SQLException {

        PreparedStatement ps = cx.prepareStatement(sql.toString());
        for (int i = 0; i < values.size(); i++) {
            Pair<Object,Integer> p = values.get(i);
            ps.setObject(i+1, p.first(), p.second());
        }
        return ps;

    }
}
