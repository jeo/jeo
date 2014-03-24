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
package org.jeo.geopkg;

import static java.lang.String.format;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.jeo.data.Cursor;
import org.jeo.data.Cursors;
import org.jeo.data.Dataset;
import org.jeo.data.FileData;
import org.jeo.data.Handle;
import org.jeo.data.Query;
import org.jeo.data.QueryPlan;
import org.jeo.tile.Tile;
import org.jeo.tile.TilePyramid;
import org.jeo.tile.TilePyramidBuilder;
import org.jeo.data.Workspace;
import org.jeo.data.Cursor.Mode;
import org.jeo.feature.Feature;
import org.jeo.feature.Features;
import org.jeo.feature.Field;
import org.jeo.feature.Schema;
import org.jeo.feature.SchemaBuilder;
import org.jeo.filter.Filters;
import org.jeo.geom.Envelopes;
import org.jeo.geom.Geom;
import org.jeo.geopkg.Entry.DataType;
import org.jeo.geopkg.geom.GeoPkgGeomWriter;
import org.jeo.proj.Proj;
import org.jeo.sql.DbOP;
import org.jeo.sql.PrimaryKey;
import org.jeo.sql.PrimaryKeyColumn;
import org.jeo.sql.SQL;
import org.jeo.util.Key;
import org.jeo.util.Pair;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.SQLiteDataSource;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Provides access to a GeoPackage SQLite database.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class GeoPkgWorkspace implements Workspace, FileData {

    static Logger LOG = LoggerFactory.getLogger(GeoPackage.class);

    /** name of geopackage contents table */
    static final String GEOPACKAGE_CONTENTS = "gpkg_contents";

    /** name of geoemtry columns table */
    static final String GEOMETRY_COLUMNS = "gpkg_geometry_columns";

    /** name of geoemtry columns table */
    static final String SPATIAL_REF_SYS = "gpkg_spatial_ref_sys";

    /** name of tile matrix table */
    static final String TILE_MATRIX = "gpkg_tile_matrix";

    /** name of tile matrix set table */
    static final String TILE_MATRIX_SET = "gpkg_tile_matrix_set";

    /** creation options */
    GeoPkgOpts opts;

    /** data source */
    DataSource db;

    /** wkb writer */
    GeoPkgGeomWriter geomWriter;

    GeoPkgTypes dbtypes;

    /**
     * Creates a GeoPackage from an existing file.
     *  
     * @param dbFile The database file.
     * 
     * @throws Exception Any error occurring opening the database file. 
     */
    public GeoPkgWorkspace(GeoPkgOpts opts) throws IOException {
        this.opts = opts;
        db = createDataSource(opts);

        dbtypes = new GeoPkgTypes();
        geomWriter = new GeoPkgGeomWriter();

        init();
    }

    DataSource createDataSource(GeoPkgOpts opts) {
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite:" + opts.getFile().getPath());
        return dataSource;
    }

    void init() throws IOException {
        run(new DbOP<Void>() {
            @Override
            protected Void doRun(Connection cx) throws Exception {
              // create the necessary metadata tables
              runScript(SPATIAL_REF_SYS + ".sql", cx);
              runScript(GEOMETRY_COLUMNS + ".sql", cx);
              runScript(GEOPACKAGE_CONTENTS + ".sql", cx);
              runScript(TILE_MATRIX +".sql", cx);
              runScript(TILE_MATRIX_SET + ".sql", cx);
              return null;
            }
        });
    }

    void runScript(String file, Connection cx) throws IOException, SQLException {
        List<String> lines = readScript(file);

        Statement st = cx.createStatement();

        try {
            StringBuilder buf = new StringBuilder();
            for (String sql : lines) {
                sql = sql.trim();
                if (sql.isEmpty()) {
                    continue;
                }
                if (sql.startsWith("--")) {
                    continue;
                }
                buf.append(sql).append(" ");

                if (sql.endsWith(";")) {
                    String stmt = buf.toString();
                    boolean skipError = stmt.startsWith("?");
                    if (skipError) {
                        stmt = stmt.replaceAll("^\\? *" ,"");
                    }

                    LOG.debug(stmt);
                    st.addBatch(stmt);

                    buf.setLength(0);
                }
            }
            st.executeBatch();
        }
        finally {
            st.close();
        }
    }

    List<String> readScript(String file) throws IOException {
        InputStream in = getClass().getResourceAsStream(file);
        BufferedReader r = new BufferedReader(new InputStreamReader(in));
        try {
            List<String> lines = new ArrayList<String>();
            String line = null;
            while((line = r.readLine()) != null) {
                lines.add(line);
            }
            return lines;
        }
        finally {
            r.close();
        }
    }

    @Override
    public GeoPackage getDriver() {
        return new GeoPackage();
    }

    @Override
    public Map<Key<?>, Object> getDriverOptions() {
        return opts.toMap();
    }

    public File getFile() {
        return opts.getFile();
    }

    DataSource getDataSource() {
        return db;
    }

    @Override
    public Iterable<Handle<Dataset>> list() throws IOException {
        return run(new DbOP<List<Handle<Dataset>>>() {
            @Override
            protected List<Handle<Dataset>> doRun(Connection cx) throws Exception {
                String sql = format("SELECT table_name FROM %s", GEOPACKAGE_CONTENTS);
                log(sql);

                ResultSet rs = open(open(cx.createStatement()).executeQuery(sql));
                List<Handle<Dataset>> refs = new ArrayList<Handle<Dataset>>();
                while(rs.next()) {
                    refs.add(Handle.to(rs.getString(1), GeoPkgWorkspace.this));
                }
                return refs;
            }
        });
    }

    @Override
    public Dataset get(String layer) throws IOException {
        FeatureEntry fe = feature(layer);
        if (fe != null) {
            return new GeoPkgVector(fe, this);
        }

        TileEntry te = tile(layer);
        if (te != null) {
            return new GeoPkgTileSet(te, this);
        }

        return null;
    }

//    /**
//     * Lists all entries in the geopackage.
//     */
//    public List<Entry> entries() throws IOException {
//        List<Entry> entries = new ArrayList<Entry>();
//        entries.addAll(features());
//        entries.addAll(tiles());
//        return entries;
//    }

    /**
     * Lists all the feature entries in the geopackage.
     */
    public List<FeatureEntry> features() throws IOException {
        return run(new DbOP<List<FeatureEntry>>() {
            @Override
            protected List<FeatureEntry> doRun(Connection cx) throws Exception {
                String sql = format(
                    "SELECT a.*, b.column_name, b.geometry_type_name, b.z, b.m, " +
                           "c.organization, c.organization_coordsys_id" +
                     " FROM %s a, %s b, %s c" + 
                    " WHERE a.table_name = b.table_name" + 
                      " AND a.srs_id = c.srs_id" + 
                      " AND a.data_type = '%s'", 
                      GEOPACKAGE_CONTENTS,GEOMETRY_COLUMNS,SPATIAL_REF_SYS, DataType.Feature.value());
                log(sql);

                List<FeatureEntry> entries = new ArrayList<FeatureEntry>();

                ResultSet rs = open(open(cx.createStatement()).executeQuery(sql));
                while(rs.next()) {
                    entries.add(createFeatureEntry(rs));
                }

                return entries;
            }
        });
    }

    public FeatureEntry feature(final String name) throws IOException {
        return run(new DbOP<FeatureEntry>() {
            @Override
            protected FeatureEntry doRun(Connection cx) throws Exception {
                String sql = format(
                    "SELECT a.*, b.column_name, b.geometry_type_name, b.z, b.m" +
                     " FROM %s a, %s b" + 
                    " WHERE a.table_name = b.table_name" +
                      " AND a.table_name = ?" + 
                      " AND a.data_type = ?", 
                  GEOPACKAGE_CONTENTS, GEOMETRY_COLUMNS);
                log(sql, name, DataType.Feature);

                PreparedStatement ps = open(cx.prepareStatement(sql));
                ps.setString(1, name);
                ps.setString(2, DataType.Feature.value());

                ResultSet rs = open(ps.executeQuery());
                while(rs.next()) {
                    return createFeatureEntry(rs);
                }

                return null;
            }
        });
    }

    public long count(final FeatureEntry entry, final Query q) throws IOException {
        QueryPlan qp = new QueryPlan(q);

        if (!Envelopes.isNull(q.getBounds())) {
            return Cursors.size(cursor(entry, q));
        }

        final SQL sql = new SQL("SELECT count(*) FROM ").name(entry.getTableName());
        final List<Object> args = encodeQuery(sql, q, qp);

        if (q.isFiltered() && !qp.isFiltered()) {
            return Cursors.size(cursor(entry, q));
        }

        return run(new DbOP<Long>() {
            @Override
            protected Long doRun(Connection cx) throws Exception {
                ResultSet rs = 
                    open(open(prepareStatement(log(sql.toString()), args, cx)).executeQuery());
                rs.next();
                return q.adjustCount(rs.getLong(1));
            }
        });
    }

    public Cursor<Feature> cursor(FeatureEntry entry, Query q) throws IOException {
        try {
            Connection cx = db.getConnection();

            if (q.getMode() == Mode.APPEND) {
                return new FeatureAppendCursor(cx, entry, this);
            }

            QueryPlan qp = new QueryPlan(q);

            //TODO: handle selective fields
            SQL sqlb = new SQL("SELECT * FROM ").name(entry.getTableName());
            List<Object> args = encodeQuery(sqlb, q, qp);

            
            PreparedStatement ps = prepareStatement(log(sqlb.toString()), args, cx);

            ResultSet rs = ps.executeQuery();

            Cursor<Feature> c = new FeatureCursor(q.getMode(), rs, cx, entry, this);

            if (!Envelopes.isNull(q.getBounds())) {
                c = Cursors.intersects(c, q.getBounds());
            }

            return qp.apply(c);
        }
        catch(Exception e) {
            throw new IOException(e);
        }
    }

    List<Object> encodeQuery(SQL sql, Query q, QueryPlan qp) {
        GeoPkgFilterSQLEncoder sqlfe = new GeoPkgFilterSQLEncoder();
        sqlfe.setDbTypes(dbtypes);

        if (!Filters.isTrueOrNull(q.getFilter())) {
            try {
                String where = sqlfe.encode(q.getFilter(), null);
                sql.add(" WHERE ").add(where);
                qp.filtered();
            }
            catch(Exception e) {
                LOG.debug("Unable to natively encode filter: " + q.getFilter(), e);
            }
        }

        if (q.getLimit() != null) {
            sql.add(" LIMIT ").add(q.getLimit());
            qp.limited();
        }
        if (q.getOffset() != null) {
            //sqlite doesn't understand offset without limit
            if (q.getLimit() == null) {
                sql.add(" LIMIT -1");
            }
            sql.add(" OFFSET ").add(q.getOffset());
            qp.offsetted();
        }

        List<Object> args = new ArrayList<Object>();
        for (Pair<Object, Integer> p : sqlfe.getArgs()) {
            args.add(p.first());
        }
        return args;
    }

    void insert(final FeatureEntry entry, final Feature feature, Connection cx) throws IOException {
        run(new DbOP<Boolean>() {
            @Override
            protected Boolean doRun(Connection cx) throws Exception {
                Feature f = Features.retype(feature, schema(entry, cx));

                SQL sqlb = new SQL("INSERT INTO ").name(entry.getTableName()).add(" (");
                List<Object> objs = new ArrayList<Object>();

                for (Field fld : f.schema()) {
                    Object o = f.get(fld.getName());
                    if (o != null) {
                        sqlb.name(fld.getName()).add(", ");
                        objs.add(o);
                    }
                }

                sqlb.trim(2).add(") VALUES (");
                for(Object obj : objs) {
                    sqlb.add("?,");
                }
                sqlb.trim(1).add(")");

                String sql = sqlb.toString();
                log(sql, objs);

                return open(prepareStatement(sql, objs, cx)).execute();
            }
        }, cx);
    }

    void update(final FeatureEntry entry, final Feature feature, Connection cx) throws IOException {
        run(new DbOP<Boolean>() {
            @Override
            protected Boolean doRun(Connection cx) throws Exception {
                SQL sqlb = new SQL("UPDATE ").name(entry.getTableName()).add(" SET ");
                List<Object> objs = new ArrayList<Object>();

                for (Map.Entry<String, Object> kv : feature.map().entrySet()) {
                    Object obj = kv.getValue();
                    if (obj == null) {
                        //TODO: revisit this, this doesn't allow us to null a property
                        continue;
                    }

                    sqlb.name(kv.getKey()).add(" = ?, ");
                    objs.add(kv.getValue());
                }
               
                if (objs.isEmpty()) {
                    return false;
                }

                sqlb.trim(2);

                PrimaryKeyColumn pk = primaryKey(entry, cx).getColumns().get(0);
                sqlb.add(" WHERE ").name(pk.getName()).add(" = ?");
                objs.add(feature.getId());

                String sql = sqlb.toString();
                log(sql, objs);

                return open(prepareStatement(sql, objs, cx)).execute();
            }
        }, cx);
    }

    void delete(final FeatureEntry entry, final Feature feature, Connection cx) throws IOException {
        run(new DbOP<Boolean>() {
            @Override
            protected Boolean doRun(Connection cx) throws Exception {
                String sql = new SQL("DELETE FROM ").name(entry.getTableName()).add(" WHERE ")
                    .name(primaryKeyCol(entry, cx).getName()).add(" = ?").toString();
                List objs = Arrays.asList(feature.getId());
                log(sql, objs);

                return open(prepareStatement(sql, objs, cx)).execute();
            }
        }, cx);
    }

    public GeoPkgVector create(Schema schema) throws IOException {
        create(new FeatureEntry(), schema);
        return (GeoPkgVector) get(schema.getName());
    }

    public void create(FeatureEntry entry, Schema schema) throws IOException {
        //clone entry so we can work on it
        FeatureEntry e = new FeatureEntry();
        e.init(entry);
        e.setTableName(schema.getName());

        if (e.getGeometryColumn() != null) {
            //check it
            if (schema.field(e.getGeometryColumn()) == null) {
                throw new IllegalArgumentException(
                        format("Geometry column %s does not exist in schema", e.getGeometryColumn()));
            }
        }
        else {
            e.setGeometryColumn(findGeometryName(schema));
        }

        if (e.getIdentifier() == null) {
            e.setIdentifier(schema.getName());
        }
        if (e.getDescription() == null) {
            e.setDescription(e.getIdentifier());
        }

        //check for srid
        if (e.getSrid() == null) {
            e.setSrid(findSRID(schema));
        }
        if (e.getSrid() == null) {
            throw new IllegalArgumentException("Entry must have srid");
        }

        //check for bounds
        if (e.getBounds() == null) {
            //TODO: this is pretty inaccurate
            e.setBounds(Proj.bounds(Proj.crs(e.getSrid())));
        }
        if (e.getBounds() == null) {
            throw new IllegalArgumentException("Entry must have bounds");
        }

        if (e.getGeometryType() == null) {
            e.setGeometryType(findGeometryType(schema));
        }
        //mark changed
        e.lastChange(new Date());

        //create the feature table
        try {
            createFeatureTable(schema, e);
        } catch (Exception ex) {
            throw new IOException("Error creating feature table", ex);
        }

        try {
            addGeopackageContentsEntry(e);
        } catch (Exception ex) {
            throw new IOException("Error updating " + GEOPACKAGE_CONTENTS, ex);
        }
        
        //update the entry
        entry.init(e);
    }

    void createFeatureTable(final Schema schema, final FeatureEntry entry) throws Exception {
        run(new DbOP<Object>() {
            @Override
            protected Object doRun(Connection cx) throws Exception {
                SQL sql = new SQL("CREATE TABLE ").name(schema.getName()).add("(");
                
                sql.name(findPrimaryKeyColumnName(schema)).add(" INTEGER PRIMARY KEY, ");
                for (Field f : schema) {
                    sql.name(f.getName()).add(" ");
                    if (f.isGeometry()) {
                        sql.add(Geom.Type.from(f.getType()).getSimpleName());
                    }
                    else {
                        String t = dbtypes.toName(f.getType());
                        sql.add(t != null ? t : "TEXT");
                    }

                    sql.add(", ");
                }
                sql.trim(2).add(")");
                
                PreparedStatement ps = open(cx.prepareStatement(log(sql.toString())));
                ps.execute();

                //update geometry columns
                addGeometryColumnsEntry(schema, entry, cx);
                return null;
            }
        });
    }

    void addGeopackageContentsEntry(final FeatureEntry entry) throws Exception {
        run(new DbOP<Object>() {
            @Override
            protected Object doRun(Connection cx) throws Exception {
                //addCRS(e.getSrid());

                SQL sqlb = new SQL("INSERT INTO").add(" %s ", GEOPACKAGE_CONTENTS)
                    .add("(table_name, data_type, identifier");

                StringBuilder vals = new StringBuilder("VALUES (?,?,?");

                if (entry.getDescription() != null) {
                    sqlb.add(", description");
                    vals.append(",?");
                }

                if (entry.getLastChange() != null) {
                    sqlb.add(", last_change");
                    vals.append(",?");
                }
                if (entry.getBounds() != null) {
                    sqlb.add(", min_x, min_y, max_x, max_y");
                    vals.append(",?,?,?,?");
                }
                
                if (entry.getSrid() != null) {
                    sqlb.add(", srs_id");
                    vals.append(",?");
                }
                sqlb.add(") ").add(vals.append(")").toString());

                PreparedStatement ps = open(cx.prepareStatement(log(sqlb.toString(), entry)));
                ps.setString(1, entry.getTableName());
                ps.setString(2, entry.getDataType().value());
                ps.setString(3, entry.getIdentifier());

                int i = 4;
                if (entry.getDescription() != null) {
                    ps.setString(i++, entry.getDescription());
                }

                if (entry.getLastChange() != null) {
                    ps.setString(i++, entry.getLastChange());
                }
                if (entry.getBounds() != null) {
                    Envelope b = entry.getBounds();
                    ps.setDouble(i++, b.getMinX());
                    ps.setDouble(i++, b.getMinY());
                    ps.setDouble(i++, b.getMaxX());
                    ps.setDouble(i++, b.getMaxY());
                }

                if(entry.getSrid() != null) {
                    ps.setInt(i++, entry.getSrid());
                }
                
                ps.executeUpdate();
                return null;
            }
        });
    }

    void addGeometryColumnsEntry(final Schema schema, final FeatureEntry entry, Connection cx) 
        throws Exception {
        run(new DbOP<Object>() {
            @Override
            protected Object doRun(Connection cx) throws Exception {
                String sql = format(
                    "INSERT INTO %s VALUES (?, ?, ?, ?, ?, ?);", GEOMETRY_COLUMNS);
                
                log(sql, entry.getTableName(), entry.getGeometryColumn(), 
                    entry.getGeometryType(), entry.getSrid(), entry.hasZ(), entry.hasM());

                PreparedStatement ps = open(cx.prepareStatement(sql));
                ps.setString(1, entry.getTableName());
                ps.setString(2, entry.getGeometryColumn());
                ps.setString(3, entry.getGeometryType().getSimpleName());
                ps.setInt(4, entry.getSrid());
                ps.setBoolean(5, entry.hasZ());
                ps.setBoolean(6, entry.hasM());

                ps.executeUpdate();
                return null;
            }
        }, cx);
    }

    String findPrimaryKeyColumnName(Schema schema) {
        String[] names = new String[]{"fid", "gid", "oid"};
        for (String name : names) {
            if (schema.field(name) == null && schema.field(name.toUpperCase()) == null) {
                return name;
            }
        }
        return null;
    }

    Integer findSRID(Schema schema) {
        CoordinateReferenceSystem crs = schema.crs();
        return crs != null ? Proj.epsgCode(crs) : null;
    }

    Geom.Type findGeometryType(Schema schema) {
        Field geom = schema.geometry();
        return geom != null ? Geom.Type.from(geom.getType()) : null;
    }

    String findGeometryName(Schema schema) {
        Field geom = schema.geometry();
        return geom != null ? geom.getName() : null;
    }

    FeatureEntry createFeatureEntry(ResultSet rs) throws Exception {
        FeatureEntry e = new FeatureEntry();

        initEntry(e, rs);
        e.setGeometryColumn(rs.getString(11));
        e.setGeometryType(Geom.Type.from(rs.getString(12)));
        e.setZ((rs.getBoolean(13)));
        e.setM((rs.getBoolean(14)));
        return e;
    }

    public Schema schema(FeatureEntry entry, Connection cx) throws IOException {
        if (entry.getSchema() == null) {
            try {
                entry.setSchema(createSchema(entry, cx));
            } catch (Exception e) {
                throw new IOException(e);
            }
        }
        return entry.getSchema();
    }

    public PrimaryKey primaryKey(FeatureEntry entry, Connection cx) throws IOException {
        if (entry.getPrimaryKey() == null) {
            try {
                entry.setPrimaryKey(createPrimaryKey(entry, cx));
            }
            catch(Exception e) {
                throw new IOException(e);
            }
        }
        return entry.getPrimaryKey();
    }
 
    public PrimaryKeyColumn primaryKeyCol(FeatureEntry entry, Connection cx) throws IOException {
        // geopackage spec mandates a single primary key column in all cases, but we could be safe
        // and do a check anyways
        return primaryKey(entry, cx).getColumns().get(0);
    }

    Schema createSchema(final FeatureEntry entry, Connection cx) throws Exception {
        return run(new DbOP<Schema>() {
            @Override
            protected Schema doRun(Connection cx) throws Exception {
                String tableName = entry.getTableName();

                String sql = format("SELECT * FROM %s LIMIT 1", tableName);
                log(sql);

                ResultSet rs = open(open(cx.createStatement()).executeQuery(sql));
                ResultSetMetaData rsmd = rs.getMetaData();

                SchemaBuilder sb = Schema.build(entry.getTableName());
                for (int i = 0; i < rsmd.getColumnCount(); i++) {
                    String col = rsmd.getColumnName(i+1);
                    if (col.equals(entry.getGeometryColumn())) {
                        CoordinateReferenceSystem crs = entry.getSrid() != null ? 
                            Proj.crs(entry.getSrid()) : null;
                        sb.field(col, entry.getGeometryType().getType(), crs);
                    }
                    else {
                        sb.field(col, dbtypes.fromSQL(rsmd.getColumnType(i+1)));
                    }
                }
                return sb.schema();
            }
        }, cx);
    }

    PrimaryKey createPrimaryKey(final FeatureEntry entry, Connection cx) throws Exception {
        final Schema schema = schema(entry, cx);

        return run(new DbOP<PrimaryKey>() {
            @Override
            protected PrimaryKey doRun(Connection cx) throws Exception {
                DatabaseMetaData md = cx.getMetaData();
                ResultSet pk = open(md.getPrimaryKeys(null, "", entry.getTableName()));

                List<PrimaryKeyColumn> cols = new ArrayList<PrimaryKeyColumn>();
                while(pk.next()) {
                    final String name = pk.getString("COLUMN_NAME");
                    if (name == null) {
                        continue;
                    }

                    Field fld = schema.field(name);

                    PrimaryKeyColumn col = new PrimaryKeyColumn(name, fld);
                    col.setAutoIncrement(true);  // sqlite primary keys always auto increment
                    cols.add(col);
                }

                if (cols.isEmpty()) {
                    return null;
                }

                PrimaryKey pkey = new PrimaryKey();
                pkey.getColumns().addAll(cols);
                return pkey;
            }
        }, cx);
    }

    /**
     * Lists all the tile entries in the geopackage. 
     */
    public List<TileEntry> tiles() throws IOException {
        return run(new DbOP<List<TileEntry>>() {
            @Override
            protected List<TileEntry> doRun(Connection cx) throws Exception {
                String sql = format(
                    "SELECT a.*" +
                     " FROM %s a, %s b" + 
                    " WHERE a.table_name = b.table_name" + 
                      " AND a.data_type = ?", GEOPACKAGE_CONTENTS, TILE_MATRIX_SET);
                log(sql, DataType.Tile);

                PreparedStatement ps = open(cx.prepareStatement(sql));
                ps.setString(1, DataType.Tile.value());

                ResultSet rs = open(ps.executeQuery());

                List<TileEntry> entries = new ArrayList<TileEntry>();
                while(rs.next()) {
                    entries.add(createTileEntry(rs));
                }
                return entries;
            }
        });
    }

    public TileEntry tile(final String name) throws IOException {
        return run(new DbOP<TileEntry>() {
            @Override
            protected TileEntry doRun(Connection cx) throws Exception {
                String sql = format(
                    "SELECT a.*" +
                     " FROM %s a, %s b" + 
                    " WHERE a.table_name = ?" + 
                      " AND a.data_type = ?", GEOPACKAGE_CONTENTS, TILE_MATRIX_SET);

                log(sql, name, DataType.Tile);

                PreparedStatement ps = open(cx.prepareStatement(sql));
                ps.setString(1, name);
                ps.setString(2, DataType.Tile.value());
                    
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return createTileEntry(rs);
                }
                return null;
            }
        });
    }

    public Cursor<Tile> read(TileEntry entry) throws IOException  {
        return read(entry, null, null, null, null, null, null);
    }

    public Cursor<Tile> read(TileEntry entry, Integer lowZoom, Integer highZoom, 
        Integer lowCol, Integer highCol, Integer lowRow, Integer highRow) throws IOException  {

        final List<String> q = new ArrayList<String>();
        if (lowZoom != null && lowZoom > -1) {
            q.add("zoom_level >= " + lowZoom);
        }
        if (highZoom != null && highZoom > -1) {
            q.add("zoom_level <= " + lowZoom);
        }
        if (lowCol != null && lowCol > -1) {
            q.add("tile_column >= " + lowCol);
        }
        if (highCol != null && highCol > -1) {
            q.add("tile_column <= " + highCol);
        }
        if (lowRow != null && lowRow > -1) {
            q.add("tile_row >= " + lowRow);
        }
        if (highRow != null && highRow > -1) {
            q.add("tile_row <= " + highRow);
        }

        SQL sql = new SQL("SELECT zoom_level,tile_column,tile_row,tile_data FROM")
            .name(entry.getTableName());

        if (!q.isEmpty()) {
            sql.add(" WHERE ");
            for (String s : q) {
                sql.add(s).add(" AND ");
            }
            sql.trim(5);
        }

        try {
            Connection cx = db.getConnection();
            ResultSet rs = cx.createStatement().executeQuery(log(sql.toString()));
            return new TileCursor(rs, cx);
        }
        catch(Exception e) {
            throw new IOException(e);
        }
        
    }

    TileEntry createTileEntry(ResultSet rs) throws Exception {
        final TileEntry e = new TileEntry();
        initEntry(e, rs);

        run(new DbOP<Object>() {
            @Override
            protected Object doRun(Connection cx) throws Exception {
                //load all the tile matrix entries
                String sql = format(
                    "SELECT zoom_level,matrix_width,matrix_height,tile_width,tile_height," +
                           "pixel_x_size, pixel_y_size FROM %s WHERE table_name = ? " +
                    " ORDER BY zoom_level", TILE_MATRIX);
                log(sql, e.getTableName());

                PreparedStatement ps = open(cx.prepareStatement(sql));
                ps.setString(1, e.getTableName());

                ResultSet rs = open(ps.executeQuery());

                TilePyramidBuilder tpb = TilePyramid.build();
                //TODO: bounds
                if (rs.next()) {
                    tpb.tileSize(rs.getInt(4), rs.getInt(5));
                    do {
                        tpb.grid(rs.getInt(1), rs.getInt(2), rs.getInt(3));
                    }
                    while (rs.next());
                }

                e.setTilePyramid(tpb.pyramid());

                return null;
            }
        }, rs.getStatement().getConnection());

        return e;
    }

    /**
     * Sets common attributes of an entry. 
     */
    void initEntry(Entry e, ResultSet rs) throws Exception {
        e.setTableName(rs.getString(1));
        e.setIdentifier(rs.getString(3));
        e.setDescription(rs.getString(4));
        e.setLastChange(rs.getString(5));
        e.setBounds(new Envelope(
            rs.getDouble(6), rs.getDouble(8), rs.getDouble(7), rs.getDouble(9)));
        e.setSrid(rs.getInt(10));
    }

    PreparedStatement prepareStatement(String sql, List<Object> args, Connection cx) 
        throws SQLException, IOException {

        PreparedStatement ps = cx.prepareStatement(sql);

        for (int i = 0; i < args.size(); i++) {
            Object obj = args.get(i);
            int j = i+1;
            if (obj == null) {
                ps.setNull(j, Types.VARCHAR); 
            }
            else {
                if (obj instanceof Geometry) {
                    ps.setBytes(j, geomWriter.write((Geometry)obj));
                }
                else {
                    if (obj instanceof Byte || obj instanceof Short || obj instanceof Integer) {
                        ps.setInt(j, ((Number)obj).intValue());
                    }
                    else if (obj instanceof Long) {
                        ps.setLong(j, ((Number)obj).longValue());
                    }
                    else if (obj instanceof Float || obj instanceof Double) {
                        ps.setDouble(j, ((Number)obj).doubleValue());
                    }
                    else {
                        ps.setString(j, obj.toString());
                    }
                }
            }
        }

        return ps;
    }

    <T> T run(DbOP<T> op) throws IOException {
        return op.run(db);
    }

    <T> T run(DbOP<T> op, Connection cx) throws IOException {
        return cx != null ? op.run(cx) : run(op);
    }

    String log(String sql, Object... params) {
        if (LOG.isDebugEnabled()) {
            if (params.length == 1 && params[0] instanceof Collection) {
                params = ((Collection)params[0]).toArray(); 
            }

            StringBuilder log = new StringBuilder(sql);
            if (params.length > 0) {
                log.append("; ");
                for (Object p : params) {
                    log.append(p).append(", ");
                }
                log.setLength(log.length()-2);
            }
            LOG.debug(log.toString());
        }
        return sql;
    }

    @Override
    /**
     * Closes the geopackage and the underlying database connection.
     * <p>
     * The application should always be sure to call this method when the 
     * geopackage is no longer needed.  
     * </p> 
     */
    public void close() {
        try {
            if (db != null) {
                //db.close();
                db = null;
            }
        } catch (Exception e) {
            LOG.warn("Error disposing GeoPackage", e);
        }
    }

    protected void finalize() throws Throwable {
        close();
    }
}
