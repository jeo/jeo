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
import org.jeo.data.DatasetHandle;
import org.jeo.data.Driver;
import org.jeo.data.Workspace;
import org.jeo.feature.Field;
import org.jeo.feature.Schema;
import org.jeo.feature.SchemaBuilder;
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
        dataSource.setServerName(pgopts.getHost());
        dataSource.setDatabaseName(pgopts.getDb());
        dataSource.setPortNumber(pgopts.getPort());
        dataSource.setUser(pgopts.getUser());

        if (pgopts.getPasswd() != null) {
            dataSource.setPassword(new String(pgopts.getPasswd().get()));
        }

        return dataSource;
    }

    @Override
    public Driver<?> getDriver() {
        return new PostGIS();
    }

    @Override
    public Map<Key<?>, Object> getDriverOptions() {
        return opts.toMap();
    }

    public DataSource getDataSource() {
        return db;
    }

    public PostGISTypes getDbTypes() {
        return dbtypes;
    }

    @Override
    public Iterable<DatasetHandle> list() throws IOException {
        return run(new DbOP<List<DatasetHandle>>() {
            @Override
            protected List<DatasetHandle> doRun(Connection cx) throws Exception {
                DatabaseMetaData md = cx.getMetaData();
                ResultSet tables = 
                    open(md.getTables(null, "", null, new String[]{"TABLE", "VIEW"}));

                //TODO: avoid pulling all into list
                List<DatasetHandle> l = new ArrayList<DatasetHandle>();
                while(tables.next()) {
                    String tbl = tables.getString("TABLE_NAME");
                    String schema = tables.getString("TABLE_SCHEM");
                    if (includeTable(tbl, schema)) {
                        l.add(new DatasetHandle(tbl, Dataset.class, getDriver(), 
                            PostGISWorkspace.this));
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
    public PostGISDataset get(String layer) throws IOException {
        Table table = table(layer);
        return table != null ? new PostGISDataset(table, this) : null;
    }
    
    @Override
    public PostGISDataset create(final Schema schema) throws IOException {
        run(new DbOP<Object>() {
            @Override
            protected Object doRun(Connection cx) throws Exception {
                cx.setAutoCommit(false);

                List<Pair<Field, Integer>> gcols = new ArrayList<Pair<Field,Integer>>();

                SQL sql = new SQL("CREATE TABLE ").name(schema.getName())
                    .add(" (").name(findIdColumnName(schema)).add(" SERIAL PRIMARY KEY, ");
                
                for (Field fld : schema) {
                    String typename = dbtypes.toName(fld.getType());
                    if (typename == null) {
                        Integer sqlType = dbtypes.toSQL(fld.getType());
                        if (sqlType != null) {
                            typename = lookupTypeName(sqlType, cx);
                        }
                    }

                    if (typename == null) {
                        throw new IllegalArgumentException(
                            "Unable to map field " + fld + " to database type"); 
                    }

                    sql.name(fld.getName()).add(" ");
                    if (fld.isGeometry()) {
                        Integer srid = fld.getCRS() != null ? Proj.epsgCode(fld.getCRS()) : null;
                        srid = srid != null ? srid : -1;

                        if (info.isAtLeastVersion2()) {
                            //declare all info inline
                            sql.add("Geometry(").add(Geom.Type.from(fld.getType()).getName())
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
                        Field fld = p.first();

                        List<Pair<Object,Integer>> values = new ArrayList<Pair<Object,Integer>>();
                        values.add(new Pair("", Types.VARCHAR));
                        values.add(new Pair("public", Types.VARCHAR));
                        values.add(new Pair(schema.getName(), Types.VARCHAR));
                        values.add(new Pair(fld.getName(), Types.VARCHAR));
                        values.add(new Pair(2, Types.INTEGER));
                        values.add(new Pair(p.second(), Types.INTEGER));
                        values.add(new Pair(Geom.Type.from(fld.getType()).getName(), Types.VARCHAR));

                        logQuery(sql, values);
                        open(prepareStatement(sql, values, cx)).execute();
                    }
                }

                //TODO: create spatial index
                cx.commit();
                return null;
            }
        });

        return get(schema.getName());
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

    Table table(final String layer) throws IOException {
        return run(new DbOP<Table>() {
            @Override
            protected Table doRun(Connection cx) throws Exception {
                String sql = new SQL("SELECT * FROM ").name(layer).add(" LIMIT 0").toString();
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
                ResultSet pk = cx.getMetaData().getPrimaryKeys(null, "", layer);

                Table t = new Table(layer);

                SchemaBuilder sb  = new SchemaBuilder(layer);
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
                        Class<? extends Geometry> type = lookupGeomType(layer, name, cx);
                        if (type == null) {
                            type = (Class<? extends Geometry>) binding;
                        }

                        srid = lookupSRID(layer, name, cx);

                        CoordinateReferenceSystem crs = null;
                        if (srid != null && srid > 0) {
                            sb.property("srid", srid);
                            crs = Proj.crs(srid);
                        }
                        else {
                            sb.property("srid", -1);
                            LOG.debug(
                                String.format("Unable to determine srid for %s (%s)", layer, name));
                        }

                        sb.field(name, type, crs);
                    }
                    else {
                        sb.field(name, binding);
                    }
                }

                t.setSchema(sb.schema());

                //primary key
                while(pk.next()) {
                    final String name = pk.getString("COLUMN_NAME");
                    if (name == null) {
                        continue;
                    }

                    int i = t.getSchema().indexOf(name);

                    PrimaryKeyColumn col = new PrimaryKeyColumn(name);

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
                                    String.format("%s; 1=%s, 2=%s", sql.toString(), layer, name));
                                
                                PreparedStatement ps = open(cx.prepareStatement(sql.toString()));
                                ps.setString(1, layer);
                                ps.setString(2, name);

                                ResultSet rs = open(ps.executeQuery());
                                return rs.next() ? rs.getString(1) : null;
                            }
                        }, cx);
                        
                        if (seq  != null) {
                            col.setSequence(seq);
                        }
                    }
                    t.getPrimaryKey().getColumns().add(col);
                }
                return t;
            }
        });
    }
    

    Class<? extends Geometry> lookupGeomType(final String tbl, final String col, Connection cx) 
        throws IOException {
        return run(new DbOP<Class<? extends Geometry>>() {
            @Override
            protected Class<? extends Geometry> doRun(Connection cx) throws Exception {
                String sql = "SELECT type FROM geometry_columns" +
                    " WHERE f_table_name = ? and f_geometry_column = ?";
                LOG.debug(String.format("%s; 1=%s, 2=%s", sql, tbl, col));

                PreparedStatement st = open(cx.prepareStatement(sql));
                st.setString(1, tbl);
                st.setString(2, col);

                ResultSet rs = open(st.executeQuery());
                if (rs.next()) {
                    return (Class<? extends Geometry>) dbtypes.fromName(rs.getString(1));
                }
                else if (info.hasGeography()) {
                    sql = "SELECT type FROM geography_columns" +
                        " WHERE f_table_name = ? and f_geometry_column = ?";
                    LOG.debug(String.format("%s; 1=%s, 2=%s", sql, tbl, col));
                    
                    st = open(cx.prepareStatement(sql));
                    st.setString(1, tbl);
                    st.setString(2, col);

                    rs = open(st.executeQuery());
                    return (Class<? extends Geometry>) dbtypes.fromName(rs.getString(1));
                }
                
                return null;
            }
        });
    }

    Integer lookupSRID(final String tbl, final String col, Connection cx) throws IOException {
        return run(new DbOP<Integer>() {
            @Override
            protected Integer doRun(Connection cx) throws Exception {
                //look up crs
                SQL buf = new SQL("SELECT srid, f_table_name, f_geometry_column as column " +
                    "FROM geometry_columns");

                if (info.hasGeography()) {
                    buf.add(" UNION ").add("SELECT srid, f_table_name, f_geography_column as column " +
                        "FROM geography_columns");
                }

                String sql = new SQL("SELECT a.srid, b.proj4text FROM (")
                    .add(buf.toString()).add(") a")
                    .add(" LEFT OUTER JOIN spatial_ref_sys b ON a.srid = b.srid")
                    .add(" WHERE a.f_table_name = ?")
                    .add(" AND a.column = ?").toString();

                LOG.debug(String.format("%s; 1=%s, 2=%s", sql, tbl, col));

                PreparedStatement ps = open(cx.prepareStatement(sql));
                ps.setString(1, tbl);
                ps.setString(2, col);

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
                msg.append(String.format("%d=%s", i+1, values.get(i).first()))
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
