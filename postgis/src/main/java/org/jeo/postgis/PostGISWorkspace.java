package org.jeo.postgis;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.sql.DataSource;

import org.jeo.data.Driver;
import org.jeo.data.VectorData;
import org.jeo.data.Workspace;
import org.jeo.feature.Schema;
import org.jeo.feature.SchemaBuilder;
import org.jeo.proj.Proj;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.postgresql.ds.PGPoolingDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;

public class PostGISWorkspace implements Workspace {

    static Logger LOG = LoggerFactory.getLogger(PostGIS.class);

    PGPoolingDataSource db;
    PostGISInfo info;

    public PostGISWorkspace(PostGISOpts pgopts) throws IOException {
        db = createDataSource(pgopts);
        info = new PostGISInfo(this);
    }

    static PGPoolingDataSource createDataSource(PostGISOpts pgopts) {
        PGPoolingDataSource dataSource = new PGPoolingDataSource();
        dataSource.setServerName(pgopts.getHost());
        dataSource.setDatabaseName(pgopts.getDb());
        dataSource.setPortNumber(pgopts.getPort());
        dataSource.setUser(pgopts.getUser());
        dataSource.setPassword(pgopts.getPasswd());
        return dataSource;
    }

    @Override
    public Driver<?> getDriver() {
        return new PostGIS();
    }

    public DataSource getDataSource() {
        return db;
    }

    @Override
    public Iterator<String> layers() throws IOException {
        return run(new DbOP<List<String>>() {
            @Override
            protected List<String> doRun(Connection cx) throws Exception {
                DatabaseMetaData md = cx.getMetaData();
                ResultSet tables = 
                    open(md.getTables(null, "", null, new String[]{"TABLE", "VIEW"}));

                //TODO: avoid pulling all into list
                List<String> l = new ArrayList<String>();
                while(tables.next()) {
                    String tbl = tables.getString("TABLE_NAME");
                    String schema = tables.getString("TABLE_SCHEM");
                    if (includeTable(tbl, schema)) {
                        l.add(tbl);
                    }
                }

                return l;
            }
        }).iterator();
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
        Schema schema = schema(layer);
        return schema != null ? new PostGISDataset(schema, this) : null;
    }
    
    @Override
    public VectorData create(Schema schema) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void dispose() {
        if (db != null) {
            db.close();
        }
        db = null;
    }

    Schema schema(final String layer) throws IOException {
        return run(new DbOP<Schema>() {
            @Override
            protected Schema doRun(Connection cx) throws Exception {
                String sql = new SQL("SELECT * FROM ").name(layer).add(" LIMIT 0").toString();
                LOG.debug(sql);

                Statement st = open(cx.createStatement());
                ResultSet rs = null;
                try {
                    rs = open(st.executeQuery(sql));
                }
                catch(SQLException e) {
                    return null;
                }

                SchemaBuilder sb  = new SchemaBuilder(layer);
                Integer srid = null;

                ResultSetMetaData md = rs.getMetaData();
                for(int i = 1; i < md.getColumnCount() + 1; i++) {
                    String name = md.getColumnName(i);
                    String typeName = md.getColumnTypeName(i);
                    int sqlType = md.getColumnType(i);

                    Class<?> binding = TypeMappings.fromName(typeName);
                    if (binding == null) {
                        binding = TypeMappings.fromSQL(sqlType);
                    }
                    if (binding == null) {
                        if (LOG.isDebugEnabled()) {
                            String msg = "Unable to map %s (%s, %d), falling back on Object";
                            LOG.debug(String.format(msg, name, typeName, sqlType));
                        }
                        binding = Object.class;
                    }

                    if (Geometry.class.isAssignableFrom(binding)) {
                        // try to narrow geometry type by looking up in geometry/geography columns
                        Class<? extends Geometry> type = lookupGeomType(layer, name, cx);
                        if (type == null) {
                            type = (Class<? extends Geometry>) binding;
                        }

                        srid = lookupSRID(layer, name, cx);

                        CoordinateReferenceSystem crs = null;
                        if (srid != null) {
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
                return sb.schema();
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
                    return (Class<? extends Geometry>) TypeMappings.fromName(rs.getString(1));
                }
                else if (info.geography()) {
                    sql = "SELECT type FROM geography_columns" +
                        " WHERE f_table_name = ? and f_geometry_column = ?";
                    LOG.debug(String.format("%s; 1=%s, 2=%s", sql, tbl, col));
                    
                    st = open(cx.prepareStatement(sql));
                    st.setString(1, tbl);
                    st.setString(2, col);

                    rs = open(st.executeQuery());
                    return (Class<? extends Geometry>) TypeMappings.fromName(rs.getString(1));
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

                if (info.geography()) {
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
    
    <T> T run(DbOP<T> op) throws IOException {
        return op.run(db);
    }

    <T> T run(DbOP<T> op, Connection cx) throws IOException {
        return op.run(cx);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        dispose();
    }
}
