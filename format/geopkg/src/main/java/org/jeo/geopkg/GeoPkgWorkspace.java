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


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.jeo.filter.Filters;
import org.jeo.geom.Envelopes;
import org.jeo.geom.Geom;
import org.jeo.geopkg.Entry.DataType;
import org.jeo.proj.Proj;
import org.jeo.sql.PrimaryKey;
import org.jeo.sql.PrimaryKeyColumn;
import org.jeo.sql.SQL;
import org.jeo.util.Key;
import org.jeo.util.Pair;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;
import static java.lang.String.format;
import org.jeo.data.Transaction;
import org.jeo.feature.SchemaBuilder;
import org.jeo.geopkg.Backend.Session;
import org.jeo.geopkg.Backend.Results;

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

    Backend backend;

    /** creation options */
    GeoPkgOpts opts;

    /**
     * Creates a GeoPackage from an existing file.
     *  
     * @param dbFile The database file.
     * 
     * @throws Exception Any error occurring opening the database file. 
     */
    public GeoPkgWorkspace(Backend backend, GeoPkgOpts opts) throws IOException {
        this.backend = backend;
        this.opts = opts;

        init();
    }

    protected void init() throws IOException {
        if (backend.canRunScripts()) {
            backend.runScripts(
                // create the necessary metadata tables
                SPATIAL_REF_SYS + ".sql",
                GEOMETRY_COLUMNS + ".sql",
                GEOPACKAGE_CONTENTS + ".sql",
                TILE_MATRIX +".sql",
                TILE_MATRIX_SET + ".sql"
            );
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

    @Override
    public Iterable<Handle<Dataset>> list() throws IOException {
        Results rs = backend.query("SELECT table_name FROM %s", GEOPACKAGE_CONTENTS);
        List<Handle<Dataset>> refs = new ArrayList<Handle<Dataset>>();
        try {
            while (rs.next()) {
                refs.add(Handle.to(rs.getString(0), this));
            }
        } finally {
            rs.close();
        }
        return refs;
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
     * Lists all the feature entries in the geopackage.
     */
    public List<FeatureEntry> features() throws IOException {
        Results rs = backend.query("SELECT a.*, b.column_name, b.geometry_type_name, b.z, b.m, "
                + "c.organization, c.organization_coordsys_id"
                + " FROM %s a, %s b, %s c"
                + " WHERE a.table_name = b.table_name"
                + " AND a.srs_id = c.srs_id"
                + " AND a.data_type = '%s'",
                GEOPACKAGE_CONTENTS, GEOMETRY_COLUMNS, SPATIAL_REF_SYS, DataType.Feature.value());
        List<FeatureEntry> entries = new ArrayList<FeatureEntry>();
        try {
            while (rs.next()) {
                entries.add(backend.createFeatureEntry(rs));
            }
        } finally {
            rs.close();
        }
        return entries;
    }

    public FeatureEntry feature(final String name) throws IOException {
        String sql = format(
                    "SELECT a.*, b.column_name, b.geometry_type_name, b.z, b.m" +
                     " FROM %s a, %s b" +
                    " WHERE a.table_name = b.table_name" +
                      " AND a.table_name = ?" +
                      " AND a.data_type = ?",
                  GEOPACKAGE_CONTENTS, GEOMETRY_COLUMNS);
        Results rs = backend.queryPrepared(sql, name, DataType.Feature.value());
        FeatureEntry feature = null;
        try {
            if (rs.next()) {
                feature = backend.createFeatureEntry(rs);
            }
        } finally {
            rs.close();
        }
        return feature;
    }

    public long count(final FeatureEntry entry, final Query q) throws IOException {
        QueryPlan qp = new QueryPlan(q);

        if (!Envelopes.isNull(q.getBounds())) {
            return Cursors.size(cursor(entry, q));
        }

        final SQL sql = new SQL("SELECT count(*) FROM ").name(entry.getTableName());
        Session session = backend.session();
        // if filter refers to properties not in the schema, defer to CQL filter
        final List<Object> args = missingProperties(entry, q, session) ?
                Collections.EMPTY_LIST : encodeQuery(sql, q, qp, primaryKey(entry, session));

        if (q.isFiltered() && !qp.isFiltered()) {
            return Cursors.size(cursor(entry, q));
        }

        Results rs = session.queryPrepared(sql.toString(), args.toArray());
        long count;
        if (!rs.next()) {
            throw new IOException("expected to find a result");
        }
        try {
            count = rs.getLong(0);
        } finally {
            backend.closeSafe(rs);
            backend.closeSafe(session);
        }
        return count;
    }

    public Cursor<Feature> cursor(FeatureEntry entry, Query q) throws IOException {
        // session to use for read queries. db seems to lock things up when
        // using our transaction session for reads
        Session session = backend.session();
        // session for writing - if no transaction, the same session
        Session transaction;
        if (q.getTransaction() != Transaction.NULL) {
            transaction = ((GeoPkgTransaction) q.getTransaction()).session;
        } else {
            transaction = session;
        }
        boolean usingTransaction = session != transaction;

        Schema schema = schema(entry, session);

        if (q.getMode() == Mode.APPEND) {
            // if session != transaction, tell the cursor not to close the session
            return new FeatureAppendCursor(transaction, entry, this, schema, usingTransaction);
        }

        QueryPlan qp = new QueryPlan(q);
        PrimaryKey pk = primaryKey(entry, session);
        SQL sqlb = new SQL("SELECT ");

        List<String> queryFields = q.getFields(schema);
        // working set of fields in query
        if (queryFields.isEmpty()) {
            sqlb.add(" * ");
        } else {
            ArrayList<String> fields = new ArrayList<String>(queryFields);
            // add any primary key columns if not already there
            // these will get added to the end and the cursor will know to find
            // them there
            for (PrimaryKeyColumn pkc : pk.getColumns()) {
                if (!fields.contains(pkc.getName())) {
                    fields.add(pkc.getName());
                }
            }
            for (String f : fields) {
                sqlb.name(f).add(", ");
            }
            sqlb.trim(2);
        }
        sqlb.add(" FROM ").name(entry.getTableName());

        // @todo if the generated SQL would reference any missing properties then
        // we cannot do a native query (until we filter them out)
        boolean missingProperties = missingProperties(entry, q, session);
        List<Object> args =  missingProperties ?
            Collections.EMPTY_LIST : encodeQuery(sqlb, q, qp, pk);
        // if no missing properties, tell the query plan we can do the fields
        if (!missingProperties) {
            qp.fields();
        }

        Results rs = transaction.queryPrepared(sqlb.toString(), args.toArray());
        // if under a transaction, close the session since we're done with it
        if (usingTransaction) {
            session.close();
        }

        // if session != transaction, tell the cursor not to close the session
        Cursor<Feature> c = new FeatureCursor(transaction, rs, q.getMode(), entry, this,
            schema, pk, usingTransaction, queryFields);

        if (!Envelopes.isNull(q.getBounds())) {
            c = Cursors.intersects(c, q.getBounds());
        }

        return qp.apply(c);
    }

    List<Object> encodeQuery(SQL sql, Query q, QueryPlan qp, PrimaryKey pk) {
        GeoPkgFilterSQLEncoder sqlfe = new GeoPkgFilterSQLEncoder();
        sqlfe.setPrimaryKey(pk);
        sqlfe.setDbTypes(backend.dbTypes);

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

        List<Object> args = new ArrayList<Object>(sqlfe.getArgs().size());
        for (Pair<Object, Integer> p : sqlfe.getArgs()) {
            args.add(p.first());
        }
        return args;
    }

    Session insert(final FeatureEntry entry, final Feature feature, Session session) throws IOException {
        if (session == null) {
            session = backend.session();
        }
        Feature f = Features.retype(feature, schema(entry, session));

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

        session.executePrepared(sqlb.toString(), objs.toArray());

        return session;
    }

    Session update(final FeatureEntry entry, final Feature feature, Session session) throws IOException {
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
            return session;
        }

        sqlb.trim(2);

        PrimaryKeyColumn pk = primaryKey(entry, session).getColumns().get(0);
        sqlb.add(" WHERE ").name(pk.getName()).add(" = ?");
        objs.add(feature.getId());

        session.executePrepared(sqlb.toString(), objs.toArray());

        return session;
    }

    Session delete(final FeatureEntry entry, final Feature feature, Session session) throws IOException {
        String sql = new SQL("DELETE FROM ").name(entry.getTableName()).add(" WHERE ")
                .name(primaryKeyCol(entry, session).getName()).add(" = ?").toString();
        List objs = Arrays.asList(feature.getId());

        session.executePrepared(sql, objs.toArray());

        return session;
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

        Session session = backend.transaction();
        try {
            boolean complete = false;
            try {
                createFeatureTable(schema, e, session);
                addGeometryColumnsEntry(schema, e, session);
                addGeopackageContentsEntry(e, session);
                complete = true;
            } finally {
                session.endTransaction(complete);
            }
        } finally {
            session.close();
        }
        
        //update the entry
        entry.init(e);
    }

    void createFeatureTable(Schema schema, FeatureEntry entry, Session session) throws IOException {
        SQL sql = new SQL("CREATE TABLE ").name(schema.getName()).add("(");

        sql.name(findPrimaryKeyColumnName(schema)).add(" INTEGER PRIMARY KEY, ");
        for (Field f : schema) {
            sql.name(f.getName()).add(" ");
            if (f.isGeometry()) {
                sql.add(Geom.Type.from(f.getType()).getSimpleName());
            } else {
                String t = backend.dbTypes.toName(f.getType());
                sql.add(t != null ? t : "TEXT");
            }

            sql.add(", ");
        }
        sql.trim(2).add(")");

        session.execute(sql.toString());
    }

    void addGeopackageContentsEntry(FeatureEntry entry, Session session) throws IOException {
        //addCRS(e.getSrid());

        SQL sqlb = new SQL("INSERT INTO").add(" %s ", GEOPACKAGE_CONTENTS)
                .add("(table_name, data_type, identifier");

        StringBuilder vals = new StringBuilder("VALUES (?,?,?");
        List<Object> args = new ArrayList<Object>();

        args.add(entry.getTableName());
        args.add(entry.getDataType().value());
        args.add(entry.getIdentifier());

        if (entry.getDescription() != null) {
            sqlb.add(", description");
            vals.append(",?");
            args.add(entry.getDescription());
        }

        if (entry.getLastChange() != null) {
            sqlb.add(", last_change");
            vals.append(",?");
            args.add(entry.getLastChange());
        }
        if (entry.getBounds() != null) {
            sqlb.add(", min_x, min_y, max_x, max_y");
            vals.append(",?,?,?,?");
            Envelope b = entry.getBounds();
            args.add(b.getMinX());
            args.add(b.getMinY());
            args.add(b.getMaxX());
            args.add(b.getMaxY());
        }

        if (entry.getSrid() != null) {
            sqlb.add(", srs_id");
            vals.append(",?");
            args.add(entry.getSrid());
        }
        sqlb.add(") ").add(vals.append(")").toString());

        session.executePrepared(sqlb.toString(), args.toArray());
    }

    void addGeometryColumnsEntry(final Schema schema, final FeatureEntry entry, Session cx)
        throws IOException {

        String sql = format(
                    "INSERT INTO %s VALUES (?, ?, ?, ?, ?, ?);", GEOMETRY_COLUMNS);

        cx.executePrepared(sql,
            entry.getTableName(),
            entry.getGeometryColumn(),
            entry.getGeometryType().getSimpleName(),
            entry.getSrid(),
            entry.hasZ(),
            entry.hasM()
        );
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

    Schema schema(FeatureEntry entry, Session ignored) throws IOException {
        return schema(entry);
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

    public PrimaryKey primaryKey(FeatureEntry entry, Session cx) throws IOException {
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
 
    public PrimaryKeyColumn primaryKeyCol(FeatureEntry entry, Session cx) throws IOException {
        // geopackage spec mandates a single primary key column in all cases, but we could be safe
        // and do a check anyways
        return primaryKey(entry, cx).getColumns().get(0);
    }

    Schema createSchema(final FeatureEntry entry) throws Exception {
        String tableName = entry.getTableName();
        SchemaBuilder sb = Schema.build(tableName);

        List<Pair<String, Class>> columnInfo = backend.getColumnInfo(tableName);

        for (int i = 0; i < columnInfo.size(); i++) {
            Pair<String, Class> col = columnInfo.get(i);
            String name = col.first();
            if (name.equals(entry.getGeometryColumn())) {
                CoordinateReferenceSystem crs = entry.getSrid() != null
                        ? Proj.crs(entry.getSrid()) : null;
                sb.field(name, entry.getGeometryType().getType(), crs);
            } else {
                sb.field(name, col.second());
            }
        }

        return sb.schema();
    }

    PrimaryKey createPrimaryKey(final FeatureEntry entry, Session cx) throws Exception {
        List<PrimaryKeyColumn> cols = new ArrayList<PrimaryKeyColumn>();
        Schema schema = schema(entry, cx);

        List<String> names = cx.getPrimaryKeys(entry.getTableName());
        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);
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

    /**
     * Lists all the tile entries in the geopackage. 
     */
    public List<TileEntry> tiles() throws IOException {
        String sql = format(
                "SELECT a.*"
                + " FROM %s a, %s b"
                + " WHERE a.table_name = b.table_name"
                + " AND a.data_type = ?", GEOPACKAGE_CONTENTS, TILE_MATRIX_SET);

        Results rs = backend.queryPrepared(sql, DataType.Tile.value());

        List<TileEntry> entries = new ArrayList<TileEntry>();
        try {
            while (rs.next()) {
                entries.add(createTileEntry(rs));
            }
        } finally {
            rs.close();
        }
        return entries;
    }

    public TileEntry tile(final String name) throws IOException {
        String sql = format(
                "SELECT a.*"
                + " FROM %s a, %s b"
                + " WHERE a.table_name = ?"
                + " AND a.data_type = ?", GEOPACKAGE_CONTENTS, TILE_MATRIX_SET);

        Results rs = backend.queryPrepared(sql, name, DataType.Tile.value());
        try {
            if (rs.next()) {
                return createTileEntry(rs);
            }
        } finally {
            rs.close();
        }
        return null;
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
            return new TileCursor(backend.query(sql.toString()));
        }
        catch(Exception e) {
            throw new IOException(e);
        }
        
    }

    TileEntry createTileEntry(Results rs) throws IOException {
        final TileEntry e = new TileEntry();
        backend.initEntry(e, rs);

        //load all the tile matrix entries
        String sql = format(
                "SELECT zoom_level,matrix_width,matrix_height,tile_width,tile_height,"
                + "pixel_x_size, pixel_y_size FROM %s WHERE table_name = ? "
                + " ORDER BY zoom_level", TILE_MATRIX);

        TilePyramidBuilder tpb = TilePyramid.build();

        try {
            rs = backend.queryPrepared(sql, e.getTableName());

            //TODO: bounds
            if (rs.next()) {
                tpb.tileSize(rs.getInt(3), rs.getInt(4));
                do {
                    tpb.grid(rs.getInt(0), rs.getInt(1), rs.getInt(2));
                } while (rs.next());
            }

            e.setTilePyramid(tpb.pyramid());
        } finally {
            rs.close();
        }

        return e;
    }

    boolean missingProperties(FeatureEntry entry, Query q, Session session) throws IOException {
        boolean hasMissing = false;
        if (q.getFilter() != null) {
            Set<String> properties = Filters.properties(q.getFilter());
            // try to defer resolving the schema unless needed
            if (!properties.isEmpty()) {
                hasMissing = !q.missingProperties(schema(entry, session)).isEmpty();
            }
        }
        return hasMissing;
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
            if (backend != null) {
                backend.close();
                backend = null;
            }
        } catch (Exception e) {
            LOG.warn("Error disposing GeoPackage", e);
        }
    }

    protected void finalize() throws Throwable {
        close();
    }

    /** for testing only **/
    Results rawQuery(String sql) throws IOException {
        return backend.query(sql);
    }
}
