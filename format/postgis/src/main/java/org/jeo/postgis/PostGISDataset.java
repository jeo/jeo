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
import org.jeo.vector.FeatureCursor;
import org.jeo.vector.VectorQuery;
import org.jeo.vector.VectorQueryPlan;
import org.jeo.vector.VectorDataset;
import org.jeo.vector.Feature;
import org.jeo.vector.Field;
import org.jeo.vector.Schema;
import org.jeo.filter.Filter;
import org.jeo.filter.Filters;
import org.jeo.geom.Envelopes;
import org.jeo.sql.DbOP;
import org.jeo.sql.FilterSQLEncoder;
import org.jeo.sql.PrimaryKey;
import org.jeo.sql.PrimaryKeyColumn;
import org.jeo.sql.SQL;
import org.jeo.sql.Table;
import org.jeo.util.Key;
import org.jeo.util.Pair;
import org.jeo.util.Util;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKTWriter;
import java.util.Set;

public class PostGISDataset implements VectorDataset {

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
    public Driver<?> driver() {
        return pg.driver();
    }

    @Override
    public Map<Key<?>, Object> driverOptions() {
        return pg.driverOptions();
    }

    @Override
    public String name() {
        return table.name();
    }

    @Override
    public String title() {
        return null;
    }

    @Override
    public String description() {
        return null;
    }

    @Override
    public Schema schema() {
        return table.type();
    }

    @Override
    public CoordinateReferenceSystem crs() {
        return schema().crs();
    }

    @Override
    public Envelope bounds() throws IOException {
        if (schema().geometry() == null) {
            return null;
        }

        return pg.run(new DbOP<Envelope>() {
            @Override
            protected Envelope doRun(Connection cx) throws Exception {
                Schema schema = schema();

                String sql = new SQL("SELECT st_asbinary(st_force_2d(st_extent(")
                    .name(schema.geometry().name()).add(")))")
                    .add(" FROM ").name(table.schema(), table.name()).toString();
                LOG.debug(sql);

                ResultSet rs = open(open(cx.createStatement()).executeQuery(sql));
                rs.next();

                byte[] wkb = rs.getBytes(1);
                return new WKBReader().read(wkb).getEnvelopeInternal();
            }
        });
    }

    @Override
    public long count(final VectorQuery q) throws IOException {
        //save original query
        VectorQueryPlan qp = new VectorQueryPlan(q);

        final SQL sql = new SQL("SELECT count(*) FROM ").name(table.schema(), table.name());
        final List<Pair<Object,Integer>> args = new ArrayList<Pair<Object,Integer>>();

        // if filter refers to properties not in the schema, defer to CQL filter
        if (!missingProperties(q)) {
            encodeQuery(sql, q, qp, args);
        }
        if (!Filters.isTrueOrNull(q.filter()) && qp.isFiltered()) {
            return pg.run(new DbOP<Long>() {
                @Override
                protected Long doRun(Connection cx) throws Exception {
                    pg.logQuery(sql, args);

                    PreparedStatement ps = open(pg.prepareStatement(sql, args, cx));

                    ResultSet rs = open(ps.executeQuery());
                    rs.next();
                    return rs.getLong(1);
                }
            });
        }
        else {
            return cursor(q).count();
        }
    }

    @Override
    public FeatureCursor cursor(VectorQuery q) throws IOException {
        try {
            VectorQueryPlan qp = new VectorQueryPlan(q);

            Connection cx = pg.getDataSource().getConnection();
            
            if (q.mode() == Cursor.APPEND) {
                return new PostGISAppendCursor(this, cx);
            }
    
            Schema schema = schema();
    
            SQL sql = new SQL("SELECT ");
            
            if (q.fields().isEmpty()) {
                //grab all from the schema
                for (Field f : schema()) {
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
                for (String prop : q.fields()) {
                    Field f = schema().field(prop);
                    if (f == null) {
                        throw new IllegalArgumentException("No such field: " + prop);
                    }
    
                    encodeFieldForSelect(f, sql);
                    sql.add(", ");
    
                    geom = geom || f.geometry();
                }
                sql.trim(2);
    
                
                if (!geom && schema.geometry() != null) {
                    encodeFieldForSelect(schema.geometry(), sql.add(", "));
                }
            }
    
            sql.add(" FROM ").name(table.schema(), table.name());

            List<Pair<Object,Integer>> args = new ArrayList<Pair<Object,Integer>>();

            // if filter refers to properties not in the schema, defer to CQL filter
            if (!missingProperties(q)) {
                encodeQuery(sql, q, qp, args);
            }

            pg.logQuery(sql, args);

            try {
                PreparedStatement st = pg.prepareStatement(sql, args, cx);
                return qp.apply(new PostGISCursor(st.executeQuery(), cx, q.mode(), this));
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
        if (f.geometry()) {
            //TODO: force 2d
            //TODO: base64 encode
            sql.add("ST_AsBinary(").name(f.name()).add(") as ").name(f.name());
        }
        else {
            sql.name(f.name());
        }
    }

    void encodeQuery(SQL sql, VectorQuery q, VectorQueryPlan qp, List<Pair<Object,Integer>> args) {
        Schema schema = schema();

        if (schema.geometry() != null && !Envelopes.isNull(q.bounds())) {
            qp.bounded();

            String geom = schema.geometry().name();
            Integer srid = schema.geometry().property("srid", Integer.class);
            
            Polygon poly = Envelopes.toPolygon(q.bounds());

            sql.add(" WHERE ").name(geom).add(" && ST_GeomFromText(?, ?)");
               //.add(" AND ST_Intersects(").name(geom).add(", ST_GeomFromText(?, ?))");

            String wkt = poly.toText();
            args.add(new Pair(wkt, Types.VARCHAR));
            args.add(new Pair(srid, Types.INTEGER));
            //values.add(new Pair(wkt, Types.VARCHAR));
            //values.add(new Pair(srid ,Types.INTEGER));
        }

        Filter<Feature> filter = q.filter();
        if (!Filters.isTrueOrNull(filter)) {
            FilterSQLEncoder sqle = new PostGISFilterEncoder(this);
            try {
                String where = sqle.encode(filter, null);
                if (args.isEmpty()) {
                    sql.add(" WHERE ");
                }
                sql.add(where);
                args.addAll(sqle.getArgs());

                qp.filtered();
            }
            catch(Exception e) {
                LOG.debug("Unable to natively encode filter", e);
            }
        }

        Integer offset = q.offset();
        if (offset != null) {
            qp.offsetted();
            sql.add(" OFFSET ").add(offset);
            //values.add(new Pair(offset, Types.INTEGER));
        }
        Integer limit = q.limit();
        if (limit != null) {
            qp.limited();
            sql.add(" LIMIT ").add(limit);
            //values.add(new Pair(limit, Types.INTEGER));
        }

    }

    void doUpdate(final Feature f, final Map<String,Object> changed, Connection cx) throws IOException {
        pg.run(new DbOP<Boolean>() {
            @Override
            protected Boolean doRun(Connection cx) throws Exception {
                Schema schema = schema();
                List<Pair<Object,Integer>> values = new ArrayList<Pair<Object,Integer>>();

                SQL sql = new SQL("UPDATE ").name(table.schema(), table.name()).add(" SET ");
                for (String col : changed.keySet()) {
                    sql.name(col).add(" = ?,");

                    Field fld = schema.field(col);
                    values.add(new Pair(f.get(col),  (Integer) fld.property("sqlType", Integer.class)));
                }
                sql.trim(1);
                sql.add(" WHERE ");

                List<PrimaryKeyColumn> pkcols = getTable().primaryKey().getColumns();
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
                    ps.setObject(i+1, p.first, p.second);
                }

                return ps.execute();
            }
        }, cx);
    }
    
    void doInsert(final Feature f, Connection cx) throws IOException {
        pg.run(new DbOP<Boolean>() {
            @Override
            protected Boolean doRun(Connection cx) throws Exception {
                Schema schema = schema();
                List<Pair<Object,Integer>> values = new ArrayList<Pair<Object,Integer>>();

                PrimaryKey pkey = getTable().primaryKey();

                SQL cols = new SQL("INSERT INTO ").name(table.schema(), table.name()).add(" (");
                SQL vals = new SQL("VALUES (");

                for (Field fld : schema) {
                    PrimaryKeyColumn pkcol = pkey.column(fld.name());
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
                            value = nextval(pkcol, fld.type(), cx);
                        }
                    }
                    else {
                        value = f.get(fld.name());
                    }

                    cols.name(fld.name()).add(",");

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
                    Object obj = p.first;
                    if (obj == null) {
                        ps.setNull(i+1, p.second);
                    }
                    else {
                        //ps.setNull(i+1, p.second());
                        ps.setObject(i+1, obj, p.second);
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
                        .name(table.schema(), table.name());

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
                "Unable to generate value for %s.%s", schema().name(), pkcol.getName()));
        }
    }

    boolean missingProperties(VectorQuery q) throws IOException {
        boolean hasMissing = false;
        if (q.filter() != null) {
            Set<String> properties = Filters.properties(q.filter());
            // try to defer resolving the schema unless needed
            if (!properties.isEmpty()) {
                hasMissing = !q.missingProperties(schema()).isEmpty();
            }
        }
        return hasMissing;
    }
}
