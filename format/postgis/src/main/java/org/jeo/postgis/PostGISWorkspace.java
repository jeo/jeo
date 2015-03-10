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

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.jeo.data.Dataset;
import org.jeo.data.Driver;
import org.jeo.data.Handle;
import org.jeo.data.Workspace;
import org.jeo.util.Optional;
import org.jeo.util.Password;
import org.jeo.vector.Field;
import org.jeo.vector.Schema;
import org.jeo.vector.SchemaBuilder;
import org.jeo.geom.Geom;
import org.jeo.proj.Proj;
import org.jeo.sql.DbOP;
import org.jeo.sql.PrimaryKeyColumn;
import org.jeo.sql.SQL;
import org.jeo.sql.Table;
import org.jeo.util.Key;
import org.jeo.util.Pair;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.postgresql.ds.PGPoolingDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;

public class PostGISWorkspace implements Workspace {

    static Logger LOG = LoggerFactory.getLogger(PostGIS.class);

    PostGISOpts opts;
    PGPoolingDataSource db;
    PostGISInfo info;
    PostGISTypes dbtypes;

    public PostGISWorkspace(PostGISOpts pgopts) throws IOException {
        opts = pgopts;
        db = createDataSource(pgopts);
        info = new PostGISInfo(this);
        dbtypes = new PostGISTypes();
    }

    static PGPoolingDataSource createDataSource(PostGISOpts pgopts) {
        PGPoolingDataSource dataSource = new PGPoolingDataSource();
        dataSource.setServerName(pgopts.host());
        dataSource.setDatabaseName(pgopts.db());
        dataSource.setPortNumber(pgopts.port());
        dataSource.setUser(pgopts.user());

        if (pgopts.passwd() != null) {
            dataSource.setPassword(Password.toString(pgopts.passwd()));
        }

        return dataSource;
    }

    @Override
    public Driver<?> driver() {
        return new PostGIS();
    }

    @Override
    public Map<Key<?>, Object> driverOptions() {
        return opts.toMap();
    }

    public DataSource getDataSource() {
        return db;
    }

    public PostGISTypes getDbTypes() {
        return dbtypes;
    }

    public Optional<String> schema() {
        return Optional.of(opts.schema());
    }

    @Override
    public Iterable<Handle<Dataset>> list() throws IOException {
        return run(new DbOP<List<Handle<Dataset>>>() {
            @Override
            protected List<Handle<Dataset>> doRun(Connection cx) throws Exception {
                DatabaseMetaData md = cx.getMetaData();
                ResultSet tables = 
                    open(md.getTables(null, schema().orElse(null), null, new String[]{"TABLE", "VIEW"}));

                //TODO: avoid pulling all into list
                List<Handle<Dataset>> l = new ArrayList<Handle<Dataset>>();
                while(tables.next()) {
                    String tbl = tables.getString("TABLE_NAME");
                    String schema = tables.getString("TABLE_SCHEM");
                    if (includeTable(tbl, schema)) {
                        l.add(new Handle<Dataset>(datasetName(tbl, schema), Dataset.class, driver()) {
                            @Override
                            protected Dataset doResolve() throws IOException {
                                return get(name);
                            }
                        });
                    }
                }

                return l;
            }
        });
    }

    boolean includeTable(String tbl, String schema) {
        if (tbl.equals("geometry_columns")) {
            return false;
        } else if (tbl.startsWith("spatial_ref_sys")) {
            return false;
        } else if (tbl.equals("geography_columns")) {
            return false;
        } else if (tbl.equals("raster_columns")) {
            return false;
        } else if (tbl.equals("raster_overviews")) {
            return false;
        }

        if (schema != null && schema.equals("topology")) {
            return false;
        }
        return true;
    }

    @Override
    public PostGISDataset get(String dataset) throws IOException {
        Table table = table(dataset);
        return table != null ? new PostGISDataset(table, this) : null;
    }
    
    @Override
    public PostGISDataset create(final Schema schema) throws IOException {
        run(new DbOP<Object>() {
            @Override
            protected Object doRun(Connection cx) throws Exception {
                cx.setAutoCommit(false);

                List<Pair<Field, Integer>> gcols = new ArrayList<Pair<Field,Integer>>();

                SQL sql = new SQL("CREATE TABLE ").name(schema().orElse(null), schema.name())
                    .add(" (").name(findIdColumnName(schema)).add(" SERIAL PRIMARY KEY, ");
                
                for (Field fld : schema) {
                    String typename = dbtypes.toName(fld.type());
                    if (typename == null) {
                        Integer sqlType = dbtypes.toSQL(fld.type());
                        if (sqlType != null) {
                            typename = lookupTypeName(sqlType, cx);
                        }
                    }

                    if (typename == null) {
                        throw new IllegalArgumentException(
                            "Unable to map field " + fld + " to database type"); 
                    }

                    sql.name(fld.name()).add(" ");
                    if (fld.isGeometry()) {
                        Integer srid = fld.crs() != null ? Proj.epsgCode(fld.crs()) : null;
                        srid = srid != null ? srid : -1;

                        if (info.isAtLeastVersion2()) {
                            //declare all info inline
                            sql.add("Geometry(").add(Geom.Type.from(fld.type()).getName())
                               .add(", ").add(srid).add(")");
                        }
                        else {
                            gcols.add(new Pair(fld, srid));
                            sql.add("Geometry");
                        }
                    }
                    else {
                        sql.add(typename);
                    }

                    sql.add(", ");
                }
                sql.trim(2).add(")");

                LOG.debug(sql.toString());

                Statement st = open(cx.createStatement());
                st.execute(sql.toString());

                if (!info.isAtLeastVersion2()) {
                    sql = new SQL("INSERT INTO geometry_columns (f_table_catalog, f_table_schema,"+
                        " f_table_name, f_geometry_column, coord_dimension, srid, type)" + 
                        " VALUES (?,?,?,?,?,?,?)");

                    //manually register geomtewry columns
                    for (Pair<Field,Integer> p : gcols) {
                        Field fld = p.first;

                        List<Pair<Object,Integer>> values = new ArrayList<Pair<Object,Integer>>();
                        values.add(new Pair("", Types.VARCHAR));
                        values.add(new Pair(schema().orElse("public"), Types.VARCHAR));
                        values.add(new Pair(schema.name(), Types.VARCHAR));
                        values.add(new Pair(fld.name(), Types.VARCHAR));
                        values.add(new Pair(2, Types.INTEGER));
                        values.add(new Pair(p.second, Types.INTEGER));
                        values.add(new Pair(Geom.Type.from(fld.type()).getName(), Types.VARCHAR));

                        logQuery(sql, values);
                        open(prepareStatement(sql, values, cx)).execute();
                    }
                }

                //TODO: create spatial index
                cx.commit();
                return null;
            }
        });

        return get(schema.name());
    }

    String findIdColumnName(Schema schema) {
        String[] names = new String[]{"fid", "gid", "jid"};
        String prefix = "";
        for (int i = 0; i < 4; i++) {
            for (String n : names) {
                String name = prefix + n;
                if (schema.field(name) == null) {
                    return name;
                }
            }
            prefix += "_";
        }

        throw new IllegalStateException("Unable to find unique name for id column");
    }

    String lookupTypeName(Integer sqlType, Connection cx) throws SQLException {
        DatabaseMetaData md = cx.getMetaData();
        ResultSet types = md.getTypeInfo();
        while(types.next()) {
            if (sqlType == types.getInt("DATA_TYPE")) {
                return types.getString("TYPE_NAME");
            }
        }

        return null;
    }

    @Override
    public void close() {
        if (db != null) {
            db.close();
        }
        db = null;
    }

    String datasetName(String tbl, String schema) {
        if (schema().isPresent() && schema().get().equals(schema)) {
            return tbl;
        }

        if (schema == null || "public".equalsIgnoreCase(schema)) {
            return tbl;
        }

        return schema + "." + tbl;
    }

    Table table(String name) throws IOException {
        final String tbl;
        final String schema;

        if (name.contains(".")) {
            String[] split = name.split("\\.");
            schema = split[0];
            tbl = split[1];
        }
        else {
            tbl = name;
            schema = schema().orElse("public");
        }

        return run(new DbOP<Table>() {
            @Override
            protected Table doRun(Connection cx) throws Exception {
                String sql = new SQL("SELECT * FROM ").name(schema, tbl).add(" LIMIT 0").toString();
                LOG.debug(sql);

                Statement st = open(cx.createStatement());
                st.setFetchSize(1);

                ResultSet rs = null;
                try {
                    rs = open(st.executeQuery(sql));
                }
                catch(SQLException e) {
                    return null;
                }

                //grab primary key info
                ResultSet pk = cx.getMetaData().getPrimaryKeys(null, schema, tbl);

                final Table t = new Table(tbl, schema);

                SchemaBuilder sb  = new SchemaBuilder(datasetName(tbl, schema));
                Integer srid = null;

                ResultSetMetaData md = rs.getMetaData();
                for(int i = 1; i < md.getColumnCount() + 1; i++) {
                    String name = md.getColumnName(i);
                    String typeName = md.getColumnTypeName(i);
                    int sqlType = md.getColumnType(i);

                    Class<?> binding = dbtypes.fromName(typeName);
                    if (binding == null) {
                        binding = dbtypes.fromSQL(sqlType);
                    }
                    if (binding == null) {
                        if (LOG.isDebugEnabled()) {
                            String msg = "Unable to map %s (%s, %d), falling back on Object";
                            LOG.debug(String.format(msg, name, typeName, sqlType));
                        }
                        binding = Object.class;
                    }

                    sb.property("sqlType", sqlType);
                    if (Geometry.class.isAssignableFrom(binding)) {
                        // try to narrow geometry type by looking up in geometry/geography columns
                        Class<? extends Geometry> type = lookupGeomType(t, name, cx);
                        if (type == null) {
                            type = (Class<? extends Geometry>) binding;
                        }

                        srid = lookupSRID(t, name, cx);

                        CoordinateReferenceSystem crs = null;
                        if (srid != null && srid > 0) {
                            sb.property("srid", srid);
                            crs = Proj.crs(srid);
                        }
                        else {
                            sb.property("srid", -1);
                            LOG.debug(
                                String.format("Unable to determine srid for %s (%s)", t.qname(), name));
                        }

                        sb.field(name, type, crs);
                    }
                    else {
                        sb.field(name, binding);
                    }
                }

                t.type(sb.schema());

                //primary key
                while(pk.next()) {
                    final String colName = pk.getString("COLUMN_NAME");
                    if (colName == null) {
                        continue;
                    }

                    int i = t.type().indexOf(colName);
                    Field fld = t.type().field(colName);

                    PrimaryKeyColumn col = new PrimaryKeyColumn(colName, fld);

                    // auto increment key?
                    if (rs.getMetaData().isAutoIncrement(i+1)) {
                        col.setAutoIncrement(true);
                    }
                    else {
                        // check for a sequence
                        String seq = PostGISWorkspace.this.run(new DbOP<String>() {
                            @Override
                            protected String doRun(Connection cx) throws Exception {
                                SQL sql = new SQL("SELECT pg_get_serial_sequence(?,?)");
                                LOG.debug(
                                    String.format("%s; 1=%s, 2=%s", sql.toString(), t.name(), colName));
                                
                                PreparedStatement ps = open(cx.prepareStatement(sql.toString()));
                                ps.setString(1, t.name());
                                ps.setString(2, colName);

                                ResultSet rs = open(ps.executeQuery());
                                return rs.next() ? rs.getString(1) : null;
                            }
                        }, cx);
                        
                        if (seq  != null) {
                            col.setSequence(seq);
                        }
                    }
                    t.primaryKey().getColumns().add(col);
                }
                return t;
            }
        });
    }
    

    Class<? extends Geometry> lookupGeomType(final Table tbl, final String col, Connection cx)
        throws IOException {
        return run(new DbOP<Class<? extends Geometry>>() {
            @Override
            protected Class<? extends Geometry> doRun(Connection cx) throws Exception {
                String sql = new SQL("SELECT type FROM geometry_columns")
                     .add(" WHERE f_table_schema = ?")
                     .add(" AND f_table_name = ?")
                     .add(" AND f_geometry_column = ?").toString();

                LOG.debug(String.format("%s; 1=%s, 2=%s, 3=%s", sql, tbl.schema(), tbl.name(), col));

                PreparedStatement st = open(cx.prepareStatement(sql));
                st.setString(1, tbl.schema());
                st.setString(2, tbl.name());
                st.setString(3, col);

                ResultSet rs = open(st.executeQuery());
                if (rs.next()) {
                    return (Class<? extends Geometry>) dbtypes.fromName(rs.getString(1));
                }
                else if (info.hasGeography()) {
                    sql = new SQL("SELECT type FROM geography_columns")
                        .add(" WHERE f_table_schema = ?")
                        .add(" AND f_table_name = ?")
                        .add(" AND f_geography_column = ?").toString();

                    LOG.debug(String.format("%s; 1=%s, 2=%s, 3=%s", sql, tbl.schema(), tbl.name(), col));
                    
                    st = open(cx.prepareStatement(sql.toString()));
                    st.setString(1, tbl.schema());
                    st.setString(2, tbl.name());
                    st.setString(3, col);

                    rs = open(st.executeQuery());
                    return (Class<? extends Geometry>) dbtypes.fromName(rs.getString(1));
                }
                
                return null;
            }
        });
    }

    Integer lookupSRID(final Table tbl, final String col, Connection cx) throws IOException {
        return run(new DbOP<Integer>() {
            @Override
            protected Integer doRun(Connection cx) throws Exception {
                //look up crs
                SQL buf = new SQL("SELECT srid, f_table_schema, f_table_name, f_geometry_column as column " +
                    "FROM geometry_columns");

                if (info.hasGeography()) {
                    buf.add(" UNION ").add("SELECT srid, f_table_schema, f_table_name, f_geography_column as column " +
                        "FROM geography_columns");
                }

                String sql = new SQL("SELECT a.srid, b.proj4text FROM (")
                    .add(buf.toString()).add(") a")
                    .add(" LEFT OUTER JOIN spatial_ref_sys b ON a.srid = b.srid")
                    .add(" WHERE a.f_table_schema = ?")
                    .add(" AND a.f_table_name = ?")
                    .add(" AND a.column = ?").toString();

                LOG.debug(String.format("%s; 1=%s, 2=%s", sql, tbl.schema(), tbl.name(), col));

                PreparedStatement ps = open(cx.prepareStatement(sql));
                ps.setString(1, tbl.schema());
                ps.setString(2, tbl.name());
                ps.setString(3, col);

                ResultSet rs = open(ps.executeQuery());
                if (rs.next()) {
                    return rs.getInt(1);
                }
                else {
                    return null;
                }
            }
        }, cx);
    }

    void logQuery(SQL sql, List<Pair<Object,Integer>> values) {
        if (LOG.isDebugEnabled()) {
            StringBuilder msg = new StringBuilder(sql.toString()).append("; ");
            for (int i = 0; i < values.size(); i++) {
                msg.append(String.format("%d=%s", i+1, values.get(i).first))
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
            ps.setObject(i+1, p.first, p.second);
        }
        return ps;

    }

    <T> T run(DbOP<T> op) throws IOException {
        return op.run(db);
    }

    <T> T run(DbOP<T> op, Connection cx) throws IOException {
        return op.run(cx);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        close();
    }
}
