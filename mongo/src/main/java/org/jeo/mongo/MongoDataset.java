package org.jeo.mongo;

import java.io.IOException;

import org.jeo.data.Cursor;
import org.jeo.data.Cursor.Mode;
import org.jeo.data.Cursors;
import org.jeo.data.Driver;
import org.jeo.data.Query;
import org.jeo.data.VectorData;
import org.jeo.feature.Feature;
import org.jeo.feature.Schema;
import org.jeo.feature.SchemaBuilder;
import org.jeo.geojson.GeoJSON;
import org.jeo.geom.Geom;
import org.jeo.proj.Proj;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

public class MongoDataset implements VectorData {

    MongoWorkspace mongo;
    DBCollection dbcol;
    MongoMapper mapper;
    Schema schema;

    MongoDataset(DBCollection dbcol, MongoWorkspace mongo) {
        this.dbcol = dbcol;
        this.mongo = mongo;
        this.schema = new SchemaBuilder(dbcol.getName()).field("geometry", Geometry.class).schema();
        this.mapper = new GeoJSONMapper(this);
    }

    public MongoMapper getMapper() {
        return mapper;
    }

    public DBCollection getCollection() {
        return dbcol;
    }

    @Override
    public Driver<?> getDriver() {
        return mongo.getDriver();
    }

    @Override
    public String getName() {
        return dbcol.getName();
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
    public CoordinateReferenceSystem getCRS() {
        return Proj.EPSG_4326;
    }

    @Override
    public Envelope bounds() throws IOException {
        return mapper.bbox(dbcol);
    }

    @Override
    public Schema getSchema() throws IOException {
        return schema;
    }

    @Override
    public long count(Query q) throws IOException {
        if (q.isAll()) {
            return dbcol.count();
        }

        if (Query.FILTER.has(q.getOptions())) {
            // we can't optimize
            return Cursors.size(q.apply(cursor(q)));
        }

        long count = 
            q.getBounds() != null ? dbcol.count(encodeBboxQuery(q.getBounds())) : dbcol.count();
        Integer offset = q.consume(Query.OFFSET, 0);
        Integer limit = q.consume(Query.LIMIT, (int) count);

        return Math.min(limit, count - offset);
    }

    @Override
    public Cursor<Feature> cursor(Query q) throws IOException {
        if (q.getMode() == Mode.APPEND) {
            return new MongoCursor(q.getMode(), null, this);
        }

        //TODO: sorting
        DBCursor dbCursor = 
            q.getBounds() != null ? dbcol.find(encodeBboxQuery(q.getBounds())) : dbcol.find();

        Integer skip = q.consume(Query.OFFSET, null);
        if (skip != null) {
            dbCursor.skip(skip);
        }

        Integer limit = q.consume(Query.LIMIT, null);
        if (limit != null) {
            dbCursor.limit(limit);
        }

        return q.apply(new MongoCursor(q.getMode(), dbCursor, this));
    }

    DBObject encodeBboxQuery(Envelope bbox) {
        Polygon p = Geom.toPolygon(bbox);
        return BasicDBObjectBuilder.start().push("geometry").push("$geoIntersects")
            .append("$geometry", JSON.parse(GeoJSON.toString(p))).get();
    }

    @Override
    public void dispose() {
    }

//    @Override
//    public void add(Feature f) throws IOException {
//        BasicDBObject obj = new BasicDBObject();
//        mapper.feature(obj, f);
//        dbcol.insert(obj);
//    }

}
