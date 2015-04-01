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
package io.jeo.mongo;

import java.io.IOException;
import java.util.Map;

import io.jeo.data.Cursor.Mode;
import io.jeo.data.Driver;
import io.jeo.vector.FeatureCursor;
import io.jeo.vector.VectorQuery;
import io.jeo.vector.VectorQueryPlan;
import io.jeo.vector.VectorDataset;
import io.jeo.vector.Schema;
import io.jeo.vector.SchemaBuilder;
import io.jeo.filter.Filters;
import io.jeo.geom.Envelopes;
import io.jeo.proj.Proj;
import io.jeo.util.Key;
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
    public Driver<?> driver() {
        return mongo.driver();
    }

    @Override
    public Map<Key<?>, Object> driverOptions() {
        return mongo.driverOptions();
    }

    @Override
    public String name() {
        return dbcol.getName();
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
    public long count(VectorQuery q) throws IOException {
        if (q.isAll()) {
            return q.adjustCount(dbcol.count());
        }

        VectorQueryPlan qp = new VectorQueryPlan(q);

        if (!Filters.isTrueOrNull(q.filter())) {
            // TODO: transform natively to filter 
            // we can't optimize
            return qp.apply(cursor(q)).count();
        }

        long count = 
            q.bounds() != null ? dbcol.count(encodeBboxQuery(q.bounds())) : dbcol.count();

        return q.adjustCount(count);
    }

    @Override
    public FeatureCursor cursor(VectorQuery q) throws IOException {
        if (q.mode() == Mode.APPEND) {
            return new MongoCursor(q.mode(), null, this);
        }

        VectorQueryPlan qp = new VectorQueryPlan(q);

        //TODO: sorting
        DBCursor dbCursor = !Envelopes.isNull(q.bounds()) ?
            dbcol.find(encodeBboxQuery(q.bounds())) : dbcol.find();
        qp.bounded();

        Integer offset = q.offset();
        if (offset != null) {
            dbCursor.skip(offset);
            qp.offsetted();
        }

        Integer limit = q.limit();
        if (limit != null) {
            dbCursor.limit(limit);
            qp.limited();
        }

        return qp.apply(new MongoCursor(q.mode(), dbCursor, this));
    }

    DBObject encodeBboxQuery(Envelope bbox) {
        return mapper().query(bbox, this);
    }

    @Override
    public void close() {
    }
}
