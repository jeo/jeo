package org.jeo.mongo;

import java.io.IOException;
import java.util.Map;

import org.jeo.data.Cursor;
import org.jeo.data.Cursor.Mode;
import org.jeo.data.Cursors;
import org.jeo.data.Driver;
import org.jeo.data.Query;
import org.jeo.data.QueryPlan;
import org.jeo.data.VectorDataset;
import org.jeo.feature.Feature;
import org.jeo.feature.Schema;
import org.jeo.feature.SchemaBuilder;
import org.jeo.filter.Filter;
import org.jeo.geom.Envelopes;
import org.jeo.proj.Proj;
import org.jeo.util.Key;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

public class MongoDataset implements VectorDataset {

    MongoWorkspace mongo;
    DBCollection dbcol;
    MongoMapper mapper;
    Schema schema;

    MongoDataset(DBCollection dbcol, MongoWorkspace mongo) {
        this.dbcol = dbcol;
        this.mongo = mongo;
        this.schema = new SchemaBuilder(dbcol.getName()).field("geometry", Geometry.class).schema();
    }

    public MongoMapper getMapper() {
        return mapper;
    }

    public void setMapper(MongoMapper mapper) {
        this.mapper = mapper;
    }

    public MongoMapper mapper() {
        return this.mapper != null ? mapper : mongo.getMapper();
    }

    public DBCollection getCollection() {
        return dbcol;
    }

    @Override
    public Driver<?> getDriver() {
        return mongo.getDriver();
    }

    @Override
    public Map<Key<?>, Object> getDriverOptions() {
        return mongo.getDriverOptions();
    }

    @Override
    public String getName() {
        return dbcol.getName();
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public CoordinateReferenceSystem crs() {
        return Proj.EPSG_4326;
    }

    @Override
    public Envelope bounds() throws IOException {
        return mapper().bbox(dbcol, this);
    }

    @Override
    public Schema schema() throws IOException {
        return schema;
    }

    @Override
    public long count(Query q) throws IOException {
        if (q.isAll()) {
            return q.adjustCount(dbcol.count());
        }

        QueryPlan qp = new QueryPlan(q);

        if (!Filter.isTrueOrNull(q.getFilter())) {
            // TODO: transform natively to filter 
            // we can't optimize
            return Cursors.size(qp.apply(cursor(q)));
        }

        long count = 
            q.getBounds() != null ? dbcol.count(encodeBboxQuery(q.getBounds())) : dbcol.count();

        return q.adjustCount(count);
    }

    @Override
    public Cursor<Feature> cursor(Query q) throws IOException {
        if (q.getMode() == Mode.APPEND) {
            return new MongoCursor(q.getMode(), null, this);
        }

        QueryPlan qp = new QueryPlan(q);

        //TODO: sorting
        DBCursor dbCursor = !Envelopes.isNull(q.getBounds()) ? 
            dbcol.find(encodeBboxQuery(q.getBounds())) : dbcol.find();
        qp.bounded();

        Integer offset = q.getOffset();
        if (offset != null) {
            dbCursor.skip(offset);
            qp.offsetted();
        }

        Integer limit = q.getLimit();
        if (limit != null) {
            dbCursor.limit(limit);
            qp.limited();
        }

        return qp.apply(new MongoCursor(q.getMode(), dbCursor, this));
    }

    DBObject encodeBboxQuery(Envelope bbox) {
        return mapper().query(bbox, this);
    }

    @Override
    public void close() {
    }
}
