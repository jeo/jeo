package org.jeo.geopkg;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.sql.DataSource;

import org.jeo.data.Cursor;
import org.jeo.data.Cursors;
import org.jeo.data.DataRef;
import org.jeo.data.Dataset;
import org.jeo.data.FileData;
import org.jeo.data.Query;
import org.jeo.data.QueryPlan;
import org.jeo.data.Tile;
import org.jeo.data.TilePyramid;
import org.jeo.data.TilePyramidBuilder;
import org.jeo.data.Workspace;
import org.jeo.data.Cursor.Mode;
import org.jeo.feature.Feature;
import org.jeo.feature.Features;
import org.jeo.feature.Field;
import org.jeo.feature.Schema;
import org.jeo.feature.SchemaBuilder;
import org.jeo.filter.Filter;
import org.jeo.geom.Envelopes;
import org.jeo.geom.Geom;
import org.jeo.geopkg.Entry.DataType;
import org.jeo.geopkg.geom.GeoPkgGeomWriter;
import org.jeo.proj.Proj;
import org.jeo.sql.DbOP;
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
    static final String GEOPACKAGE_CONTENTS = "geopackage_contents";

    /** name of geoemtry columns table */
    static final String GEOMETRY_COLUMNS = "geometry_columns";

    /** name of geoemtry columns table */
    static final String SPATIAL_REF_SYS = "spatial_ref_sys";

    /** name of tile metadata table */
    static final String TILE_TABLE_METADATA = "tile_table_metadata";

    /** name of tile matrix metadata table */
    static final String TILE_MATRIX_METADATA = "tile_matrix_metadata";

    /** date format */
    static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-mm-dd'T'HH:MM:ss.SSS'Z'");
    static {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

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
    }

    DataSource createDataSource(GeoPkgOpts opts) {
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite:" + opts.getFile().getPath());
        return dataSource;
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
    public Iterable<DataRef<Dataset>> list() throws IOException {
        return run(new DbOP<List<DataRef<Dataset>>>() {
            @Override
            protected List<DataRef<Dataset>> doRun(Connection cx) throws Exception {
                String sql = format("SELECT table_name FROM %s", GEOPACKAGE_CONTENTS);
                log(sql);

                ResultSet rs = open(open(cx.createStatement()).executeQuery(sql));
                List<DataRef<Dataset>> refs = new ArrayList<DataRef<Dataset>>();
                while(rs.next()) {
                    refs.add(new DataRef<Dataset>(Dataset.class, rs.getString(1)));
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
                    "SELECT a.*, b.f_geometry_column, b.geometry_type, b.coord_dimension, " +
                           "c.auth_name, c.auth_srid" +
                     " FROM %s a, %s b, %s c" + 
                    " WHERE a.table_name = b.f_table_name" + 
                      " AND a.srid = c.srid" + 
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
                    "SELECT a.*, b.f_geometry_column, b.geometry_type, b.coord_dimension" +
                     " FROM %s a, %s b" + 
                    " WHERE a.table_name = b.f_table_name" +
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

            Schema schema = schema(entry);

            QueryPlan qp = new QueryPlan(q);

            //TODO: handle selective fields
            SQL sqlb = new SQL("SELECT * FROM ").name(entry.getTableName());
            List<Object> args = encodeQuery(sqlb, q, qp);

            
            PreparedStatement ps = prepareStatement(log(sqlb.toString()), args, cx);

            ResultSet rs = ps.executeQuery();

            Cursor<Feature> c = new FeatureCursor(rs, cx, schema);

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

        if (!Filter.isTrueOrNull(q.getFilter())) {
            try {
                sql.add(" WHERE ").add(sqlfe.encode(q.getFilter(), null));
                qp.filtered();
            }
            catch(Exception e) {
                LOG.debug("Unable to natively encode filter: " + q.getFilter(), e);
            }
        }

        if (q.getLimit() != null) {
            sql.add(" LIMIT ").add(q.getLimit());
            qp.offsetted();
        }
        if (q.getOffset() != null) {
            sql.add(" OFFSET ").add(q.getOffset());
            qp.limited();
        }

        List<Object> args = new ArrayList<Object>();
        for (Pair<Object, Integer> p : sqlfe.getArgs()) {
            args.add(p.first());
        }
        return args;
    }

    public void add(final FeatureEntry entry, final Feature feature) throws IOException {
        run(new DbOP<Boolean>() {
            @Override
            protected Boolean doRun(Connection cx) throws Exception {
                Feature f = Features.retype(feature, schema(entry));

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
        });
        
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

        if (e.getCoordDimension() == null) {
            e.setCoordDimension(2);
        }

        if (e.getGeometryType() == null) {
            e.setGeometryType(findGeometryType(schema));
        }
        //mark changed
        e.setLastChange(new Date());

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
                    sqlb.add(", srid");
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
                    ps.setString(i++, DATE_FORMAT.format(entry.getLastChange()));
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
                    "INSERT INTO %s VALUES (?, ?, ?, ?, ?);", GEOMETRY_COLUMNS);
                
                log(sql, entry.getTableName(), entry.getGeometryColumn(), 
                    entry.getGeometryType(), entry.getCoordDimension(), entry.getSrid());

                PreparedStatement ps = open(cx.prepareStatement(sql));
                ps.setString(1, entry.getTableName());
                ps.setString(2, entry.getGeometryColumn());
                ps.setString(3, entry.getGeometryType().getSimpleName());
                ps.setInt(4, entry.getCoordDimension());
                ps.setInt(5, entry.getSrid());

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
        e.setCoordDimension((rs.getInt(13)));
        return e;
    }

    public Schema schema(FeatureEntry entry) throws IOException {
        if (entry.getSchema() == null) {
            try {
                entry.setSchema(createSchema(entry));
            } catch (Exception e) {
                throw new IOException(e);
            }
        }
        return entry.getSchema();
    }

    Schema createSchema(final FeatureEntry entry) throws Exception {
        return run(new DbOP<Schema>() {
            @Override
            protected Schema doRun(Connection cx) throws Exception {
                String sql = format("SELECT * FROM %s LIMIT 1", entry.getTableName());
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
        });
    }

    /**
     * Lists all the tile entries in the geopackage. 
     */
    public List<TileEntry> tiles() throws IOException {
        return run(new DbOP<List<TileEntry>>() {
            @Override
            protected List<TileEntry> doRun(Connection cx) throws Exception {
                String sql = format(
                    "SELECT a.*, b.is_times_two_zoom" +
                     " FROM %s a, %s b" + 
                    " WHERE a.table_name = b.t_table_name" + 
                      " AND a.data_type = ?", GEOPACKAGE_CONTENTS, TILE_TABLE_METADATA);
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
                    "SELECT a.*, b.is_times_two_zoom" +
                     " FROM %s a, %s b" + 
                    " WHERE a.table_name = ?" + 
                      " AND a.data_type = ?", GEOPACKAGE_CONTENTS, TILE_TABLE_METADATA);

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

        e.setTimesTwoZoom(1 == rs.getInt(10));

        run(new DbOP<Object>() {
            @Override
            protected Object doRun(Connection cx) throws Exception {
                //load all the tile matrix entries
                String sql = format(
                    "SELECT zoom_level,matrix_width,matrix_height,tile_width,tile_height," +
                           "pixel_x_size, pixel_y_size FROM %s WHERE t_table_name = ? " +
                    " ORDER BY zoom_level", TILE_MATRIX_METADATA);
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

        try {
            e.setLastChange(DATE_FORMAT.parse(rs.getString(5)));
        } catch (ParseException ex) {
            throw new RuntimeException(ex);
        }
        e.setBounds(new Envelope(
            rs.getDouble(6), rs.getDouble(8), rs.getDouble(7), rs.getDouble(9)));
        e.setSrid(rs.getInt(10));
    }

    PreparedStatement prepareStatement(String sql, List<Object> args, Connection cx) 
        throws SQLException, IOException {

        PreparedStatement ps = cx.prepareStatement(sql);

        for (int i = 0; i < args.size(); i++) {
            Object obj = args.get(i);
            if (obj instanceof Geometry) {
                ps.setBytes(i+1, geomWriter.write((Geometry)obj));
            }
            else {
                if (obj instanceof Byte || obj instanceof Short || obj instanceof Integer) {
                    ps.setInt(i+1, ((Number)obj).intValue());
                }
                if (obj instanceof Long) {
                    ps.setLong(i+1, ((Number)obj).longValue());
                }
                if (obj instanceof Float || obj instanceof Double) {
                    ps.setDouble(i+1, ((Number)obj).doubleValue());
                }
                else {
                    ps.setString(i+1, obj.toString());
                }
            }
        }

        return ps;
    }

    <T> T run(DbOP<T> op) throws IOException {
        return op.run(db);
    }

    <T> T run(DbOP<T> op, Connection cx) throws IOException {
        return op.run(cx);
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
