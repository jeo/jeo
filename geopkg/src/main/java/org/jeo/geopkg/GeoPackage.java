package org.jeo.geopkg;

import static java.lang.String.format;
import static jsqlite.Constants.SQLITE_OPEN_CREATE;
import static jsqlite.Constants.SQLITE_OPEN_READWRITE;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import jsqlite.Database;
import jsqlite.Exception;
import jsqlite.Stmt;
import jsqlite.TableResult;

import org.jeo.data.Cursor;
import org.jeo.data.FileWorkspaceFactory;
import org.jeo.data.Dataset;
import org.jeo.data.Tile;
import org.jeo.data.TileGrid;
import org.jeo.data.Workspace;
import org.jeo.data.Workspaces;
import org.jeo.feature.Feature;
import org.jeo.feature.Features;
import org.jeo.feature.Field;
import org.jeo.feature.Schema;
import org.jeo.geom.Geom;
import org.jeo.geopkg.Entry.DataType;
import org.jeo.proj.Proj;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKBWriter;

/**
 * Provides access to a GeoPackage SQLite database.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class GeoPackage implements Workspace {

    static {
        Workspaces.registerWorkspaceFactory(new FileWorkspaceFactory<Workspace>("geopkg") {
            @Override
            protected Workspace createFromFile(File file) throws IOException {
                return new GeoPackage(file);
            }
        });
    }

    static enum Types {
        INTEGER(Integer.class, Long.class, Short.class, Byte.class, Boolean.class),
        REAL(Double.class, Float.class),
        TEXT(String.class),
        BLOB(byte[].class);

        Set<Class<?>> classes;
        Types(Class<?>... classes) {
            this.classes = new HashSet<Class<?>>(Arrays.asList(classes));
        }

        Class<?> getPrimaryClass() {
            return classes.iterator().next();
        }

        static Types forClass(Class<?> clazz) {
            for (Types t : values() ) {
                if (t.classes.contains(clazz)) {
                    return t;
                }
            }
            return null;
        }

        static Types forName(String name) {
            for (Types t : values()) {
                if (t.name().equals(name)) {
                    return t;
                }
            }
            return null;
        }
    }

    Logger LOG = LoggerFactory.getLogger(GeoPackage.class);

    /** name of geopackage contents table */
    static final String GEOPACKAGE_CONTENTS = "geopackage_contents";

    /** name of geoemtry columns table */
    static final String GEOMETRY_COLUMNS = "geometry_columns";

    /** name of tile metadata table */
    static final String TILE_TABLE_METADATA = "tile_table_metadata";

    /** name of tile matrix metadata table */
    static final String TILE_MATRIX_METADATA = "tile_matrix_metadata";

    /** date format */
    static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-mm-dd'T'HH:MM:ss.SSS'Z'");
    static {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    /** database file */
    File file;

    /** database handle */
    Database db;

    /** wkb writer */
    WKBWriter wkbWriter;

    /**
     * Creates a GeoPackage from an existing file.
     *  
     * @param dbFile The database file.
     * 
     * @throws Exception Any error occurring opening the database file. 
     */
    public GeoPackage(File file) throws IOException {
        this.file = file;
        db = new Database();
        try {
            db.open(file.getAbsolutePath(), SQLITE_OPEN_READWRITE | SQLITE_OPEN_CREATE);
        } catch (Exception e) {
            throw new IOException(e);
        }

        wkbWriter = new WKBWriter();
    }

    File getFile() {
        return file;
    }

    Database getDatabase() {
        return db;
    }

    @Override
    public Iterator<String> layers() throws IOException {
        List<String> names = new ArrayList<String>();
        for (Entry e : entries()) {
            names.add(e.getTableName());
        }
        return names.iterator();
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

    /**
     * Lists all entries in the geopackage.
     */
    public List<Entry> entries() throws IOException {
        List<Entry> entries = new ArrayList<Entry>();
        entries.addAll(features());
        entries.addAll(tiles());
        return entries;
    }

    /**
     * Lists all the feature entries in the geopackage.
     */
    public List<FeatureEntry> features() throws IOException {
        String sql = format(
            "SELECT a.*, b.f_geometry_column, b.type, b.coord_dimension" +
             " FROM %s a, %s b" + 
            " WHERE a.table_name = b.f_table_name" + 
              " AND a.data_type = ?", 
          GEOPACKAGE_CONTENTS, GEOMETRY_COLUMNS);
        log(sql, DataType.Feature);

        try {
            List<FeatureEntry> entries = new ArrayList<FeatureEntry>();

            Stmt st = db.prepare(sql);
            st.bind(1, DataType.Feature.value());

            while(st.step()) {
                entries.add(createFeatureEntry(st));
            }
    
            st.close();

            return entries;
        }
        catch(Exception e) {
            throw new IOException(e);
        }
    }

    public FeatureEntry feature(String name) throws IOException {
        String sql = format(
                "SELECT a.*, b.f_geometry_column, b.type, b.coord_dimension" +
                 " FROM %s a, %s b" + 
                " WHERE a.table_name = b.f_table_name" +
                  " AND a.table_name = ?" + 
                  " AND a.data_type = ?", 
              GEOPACKAGE_CONTENTS, GEOMETRY_COLUMNS);

        log(sql, name, DataType.Feature);
        try {
            Stmt st = db.prepare(sql);
            st.bind(1, name);
            st.bind(2, DataType.Feature.value());

            try {
                while(st.step()) {
                    return createFeatureEntry(st);
                }
            }
            finally {
                st.close();
            }
        }
        catch(Exception e) {
            throw new IOException(e);
        }
        return null;
    }

    public long count(FeatureEntry entry, Envelope bbox) throws IOException {
        try {
            SQLBuilder sql = new SQLBuilder("SELECT COUNT(*) FROM ").name(entry.getTableName());
            if (bbox != null) {
                sql.add(" WHERE ");
                encodeBBOX(sql, entry.getSchema(), bbox);
            }

            Stmt st = db.prepare(log(sql.toString()));
            st.step();
            try {
                return st.column_long(0);
            }
            finally {
                st.close();
            }
        }
        catch(Exception e) {
            throw new IOException(e);
        }
    }

    public Cursor<Feature> read(FeatureEntry entry, Envelope bbox) throws IOException {
        try {
            Schema schema = schema(entry);
            Field geom = schema.geometry();

            SQLBuilder sql = new SQLBuilder("SELECT ");

            for (Field f : schema) {
                if (Geometry.class.isAssignableFrom(f.getType())) {
                    sql.add("AsBinary(").name(geom.getName()).add("),");
                }
                else {
                    sql.name(f.getName()).add(",");
                }
            }
            sql.trim(1);
            sql.add("FROM ").name(entry.getTableName());

            if (bbox != null) {
                sql.add(" WHERE ");
                encodeBBOX(sql, schema, bbox);
            }
    
            return new FeatureCursor(db.prepare(log(sql.toString())), schema);
        }
        catch(Exception e) {
            throw new IOException(e);
        }
    }

    public void add(FeatureEntry entry, Feature f) throws IOException {
        try {
            f = Features.retype(f, schema(entry));

            SQLBuilder sql = new SQLBuilder("INSERT INTO ").name(entry.getTableName()).add(" (");
            StringBuilder vals = new StringBuilder();
            List<Object> objs = new ArrayList<Object>();

            for (Field fld : f.schema()) {
                Object o = f.get(fld.getName());
                if (o != null) {
                    sql.name(fld.getName()).add(", ");

                    if (o instanceof Geometry) {
                        vals.append("GeomFromWKB(?), ");
                    }
                    else {
                        
                        vals.append("?, ");
                    }

                    objs.add(o);
                }
            }

            if (vals.length() > 0) {
                sql.trim(2);
                vals.setLength(vals.length()-2);
            }

            sql.add(") VALUES (").add(vals.toString()).add(")");

            Stmt st = db.prepare(log(sql.toString(), objs.toArray()));
            for (int i = 0; i < objs.size(); i++) {
                Object obj = objs.get(i);
                if (obj instanceof Geometry) {
                    st.bind(i+1, wkbWriter.write((Geometry)obj));
                }
                else {
                    if (obj instanceof Byte || obj instanceof Short || obj instanceof Integer) {
                        st.bind(i+1, ((Number)obj).intValue());
                    }
                    if (obj instanceof Long) {
                        st.bind(i+1, ((Long)obj).longValue());
                    }
                    if (obj instanceof Float || obj instanceof Double) {
                        st.bind(i+1, ((Number)obj).doubleValue());
                    }
                    else {
                        st.bind(i+1, obj.toString());
                    }
                }
            }

            st.step();
            st.close();
        }
        catch(Exception e) {
            throw new IOException(e);
        }
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

    void createFeatureTable(Schema schema, FeatureEntry entry) throws Exception {
        SQLBuilder sql = new SQLBuilder("CREATE TABLE ").name(schema.getName()).add("(");
        
        sql.name(findPrimaryKeyColumnName(schema)).add(" INTEGER PRIMARY KEY, ");
        for (Field f : schema) {
            sql.name(f.getName()).add(" ");
            if (f.isGeometry()) {
                sql.add(Geom.Type.fromClass(f.getType()).getSimpleName());
            }
            else {
                Types t = Types.forClass(f.getType());
                sql.add(t != null ? t.name() : Types.TEXT.name());
            }

            sql.add(", ");
        }
        sql.trim(2).add(")");

        Stmt st = db.prepare(log(sql.toString()));
        try { 
            st.step();

            //update geometry columns
            addGeometryColumnsEntry(schema, entry);

        }
        finally {
            st.close();
        }
    }

    void addGeopackageContentsEntry(FeatureEntry entry) throws Exception {
        SQLBuilder sql = new SQLBuilder(format("INSERT INTO %s", GEOPACKAGE_CONTENTS));
        sql.add("(")
           .name("table_name").add(", ")
           .name("data_type").add(",")
           .name("identifier").add(",")
           .name("description").add(",")
           .name("last_change").add(",")
           .name("min_x").add(",")
           .name("min_y").add(",")
           .name("max_x").add(",")
           .name("max_y").add(",")
           .name("srid");
        sql.add(") VALUES (?,?,?,?,?,?,?,?,?,?)");

        Stmt st = db.prepare(log(sql.toString(), entry.getTableName(), entry.getDataType(), 
            entry.getIdentifier(), entry.getDescription(), entry.getLastChange(), entry.getBounds(),
            entry.getSrid()));

        st.bind(1, entry.getTableName());
        st.bind(2, entry.getDataType().value());
        st.bind(3, entry.getIdentifier());
        st.bind(4, entry.getDescription());
        st.bind(5, DATE_FORMAT.format(entry.getLastChange()));
        st.bind(6, entry.getBounds().getMinX());
        st.bind(7, entry.getBounds().getMinY());
        st.bind(8, entry.getBounds().getMaxX());
        st.bind(9, entry.getBounds().getMaxY());
        st.bind(10, entry.getSrid());

        st.step();
        st.close();
        /*
        table_name TEXT NOT NULL PRIMARY KEY,
        data_type TEXT NOT NULL,
        identifier TEXT NOT NULL UNIQUE,
        description TEXT NOT NULL DEFAULT 'none',
        last_change TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ',CURRENT_TIMESTAMP)),
        min_x DOUBLE NOT NULL DEFAULT -180.0,
        min_y DOUBLE NOT NULL DEFAULT -90.0,
        max_x DOUBLE NOT NULL DEFAULT 180.0,
        max_y DOUBLE NOT NULL DEFAULT 90.0,
        srid INTEGER NOT NULL DEFAULT 0,
        */
    }

    void addGeometryColumnsEntry(Schema schema, FeatureEntry entry) throws Exception {
        SQLBuilder sql = new SQLBuilder(format("INSERT INTO %s", GEOMETRY_COLUMNS));
        sql.add("(")
           .name("f_table_name").add(", ")
           .name("f_geometry_column").add(",")
           .name("type").add(",")
           .name("coord_dimension").add(",")
           .name("srid").add(",")
           .name("spatial_index_enabled");
        sql.add(") VALUES (?,?,?,?,?,?)");
        
        Stmt st = db.prepare(log(sql.toString(), entry.getTableName(), entry.getGeometryColumn(), 
            entry.getGeometryType(), entry.getCoordDimension(), entry.getSrid(), false));
        st.bind(1, entry.getTableName());
        st.bind(2, entry.getGeometryColumn());
        st.bind(3, entry.getGeometryType().getSimpleName());
        st.bind(4, entry.getCoordDimension());
        st.bind(5, entry.getSrid());
        st.bind(6, 0);

        st.step();
        st.close();
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
        return geom != null ? Geom.Type.fromClass(geom.getType()) : null;
    }

    String findGeometryName(Schema schema) {
        Field geom = schema.geometry();
        return geom != null ? geom.getName() : null;
    }

    FeatureEntry createFeatureEntry(Stmt st) throws Exception {
        FeatureEntry e = new FeatureEntry();

        initEntry(e, st);
        e.setGeometryColumn(st.column_string(10));
        e.setGeometryType(Geom.Type.fromName(st.column_string(11)));
        e.setCoordDimension((st.column_int(12)));
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

    Schema createSchema(FeatureEntry entry) throws Exception {
        String sql = format("SELECT * FROM %s", entry.getTableName());
        TableResult table = db.get_table(log(sql), 1);
        
        List<Field> fields = new ArrayList<Field>();

        for (int i = 0; i < table.column.length; i++) {
            String col = table.column[i];
            Class<?> type = null;
            if (col.equals(entry.getGeometryColumn())) {
                type = entry.getGeometryType().getType();
            }
            else {
                Types t = Types.valueOf(table.types[i].toUpperCase());
                type = t != null ? t.getPrimaryClass() : Object.class;
            }

            fields.add(new Field(col, type));
        }

        return new Schema(entry.getTableName(), fields);
    }

    /**
     * Lists all the tile entries in the geopackage. 
     */
    public List<TileEntry> tiles() throws IOException {
        try {
            String sql = format(
            "SELECT a.*, b.is_times_two_zoom" +
             " FROM %s a, %s b" + 
            " WHERE a.table_name = b.t_table_name" + 
              " AND a.data_type = ?", GEOPACKAGE_CONTENTS, TILE_TABLE_METADATA);

            log(sql, DataType.Tile);

            Stmt st = db.prepare(sql);
            st.bind(1,  DataType.Tile.value());

            List<TileEntry> entries = new ArrayList<TileEntry>();
            while(st.step()) {
                entries.add(createTileEntry(st));
            }
            st.close();

            return entries;
        }
        catch(Exception e) {
            throw new IOException(e);
        }
    }

    public TileEntry tile(String name) throws IOException {
        try {
            String sql = format(
            "SELECT a.*, b.is_times_two_zoom" +
             " FROM %s a, %s b" + 
            " WHERE a.table_name = ?" + 
              " AND a.data_type = ?", GEOPACKAGE_CONTENTS, TILE_TABLE_METADATA);

            log(sql, name, DataType.Tile);

            Stmt st = db.prepare(sql);
            st.bind(1,  name);
            st.bind(2,  DataType.Tile.value());

            try {
                if (st.step()) {
                    return createTileEntry(st);
                }
            }
            finally {
                st.close();
            }
        }
        catch(Exception e) {
            throw new IOException(e);
        }

        return null;
    }

    public Cursor<Tile> read(TileEntry entry) throws IOException  {
        return read(entry, null, null, null, null, null, null);
    }

    public Cursor<Tile> read(TileEntry entry, Integer lowZoom, Integer highZoom, 
        Integer lowCol, Integer highCol, Integer lowRow, Integer highRow) throws IOException  {

        try {
            List<String> q = new ArrayList<String>();
            if (lowZoom != null) {
                q.add("zoom_level >= " + lowZoom);
            }
            if (highZoom != null) {
                q.add("zoom_level <= " + lowZoom);
            }
            if (lowCol != null) {
                q.add("tile_column >= " + lowCol);
            }
            if (highCol != null) {
                q.add("tile_column <= " + highCol);
            }
            if (lowRow != null) {
                q.add("tile_row >= " + lowRow);
            }
            if (highRow != null) {
                q.add("tile_row <= " + highRow);
            }

            SQLBuilder sql = new SQLBuilder(
                "SELECT zoom_level,tile_column,tile_row,tile_data FROM ").name(entry.getTableName());
            if (!q.isEmpty()) {
                sql.add(" WHERE ");
                for (String s : q) {
                    sql.add(s).add(" AND ");
                }
                sql.trim(5);
            }

            Stmt stmt = db.prepare(log(sql.toString()));
            return new TileCursor(stmt);
        }
        catch(Exception e) {
            throw new IOException(e);
        }
        
    }

    TileEntry createTileEntry(Stmt st) throws Exception {
        TileEntry e = new TileEntry();
        initEntry(e, st);

        e.setTimesTwoZoom(1 == st.column_int(10));

        //load all the tile matrix entries
        String sql = format("SELECT zoom_level,matrix_width,matrix_height,tile_width,tile_height," +
            "pixel_x_size, pixel_y_size FROM %s WHERE t_table_name = ?", TILE_MATRIX_METADATA);
        log(sql, e.getTableName());

        Stmt stm = db.prepare(sql);
        stm.bind(1, e.getTableName());

        while(stm.step()) {
            TileGrid m = new TileGrid(
                stm.column_int(0), stm.column_int(1), stm.column_int(2), stm.column_int(3), 
                stm.column_int(4), stm.column_double(5), stm.column_double(6));
            e.getTileMatricies().add(m);
        }

        stm.close();

        return e;
    }

    /**
     * Sets common attributes of an entry. 
     */
    void initEntry(Entry e, Stmt st) throws Exception {
        e.setTableName(st.column_string(0));
        e.setIdentifier(st.column_string(2));
        e.setDescription(st.column_string(3));
        try {
            e.setLastChange(DATE_FORMAT.parse(st.column_string(4)));
        } catch (ParseException ex) {
            throw new RuntimeException(ex);
        }
        e.setBounds(new Envelope(
            st.column_double(5), st.column_double(7), st.column_double(6), st.column_double(8)));
        e.setSrid(st.column_int(9));
    }

    SQLBuilder encodeBBOX(SQLBuilder sql, Schema schema, Envelope bbox) {
        return sql.add(" Intersects(").name(schema.geometry().getName()).add(",")
           .add(" BuildMbr(").add(bbox.getMinX()).add(",").add(bbox.getMinY()).add(",")
           .add(bbox.getMaxX()).add(",").add(bbox.getMaxY()).add("))");
    }

    String log(String sql, Object... params) {
        if (LOG.isDebugEnabled()) {
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
    public void dispose() {
        try {
            close();
        } catch (Exception e) {
            LOG.warn("Error disposing GeoPackage", e);
        }
    }

    /**
     * Closes the geopackage and the underlying database connection.
     * <p>
     * The application should always be sure to call this method when the 
     * geopackage is no longer needed.  
     * </p> 
     */
    public void close() throws Exception {
        db.close();
    }
}
