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

import io.jeo.vector.Feature;
import io.jeo.geom.Envelopes;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MapReduceCommand;
import com.mongodb.MapReduceCommand.OutputType;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Default mapper that uses a {@link Mapping} instance to map mongo objects to features.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class DefaultMapper implements MongoMapper {

    Mapping mapping;
    String bboxMap;
    String bboxReduce;

    /**
     * Default constructor created with an empty mapping.
     */
    public DefaultMapper() {
        this(new Mapping());
    }

    /**
     * Constructor taking an explicit mapping.
     */
    public DefaultMapper(Mapping mapping) {
        this.mapping = mapping;
        if (!mapping.getGeometryPaths().isEmpty()) {
            Path first = mapping.getGeometryPaths().get(0);
            this.bboxMap = Functions.bboxMap(first.join());
            this.bboxReduce = Functions.bboxReduce();
        }
    }

    @Override
    public Feature feature(DBObject obj, MongoDataset data) {
        return new MongoFeature(obj, mapping);
    }

    @Override
    public DBObject object(Feature f, MongoDataset data) {
        return ((MongoFeature)f).object();
    }

    @Override
    public Envelope bbox(DBCollection dbcol, MongoDataset data) {
        MapReduceCommand mr = new MapReduceCommand(
            dbcol, bboxMap, bboxReduce, null, OutputType.INLINE, new BasicDBObject());

        BasicDBList list = (BasicDBList) dbcol.mapReduce(mr).getCommandResult().get("results");
        DBObject bbox = (DBObject) ((DBObject) list.get(0)).get("value");

        return new Envelope((Double)bbox.get("x1"), (Double)bbox.get("x2"), 
            (Double)bbox.get("y1"), (Double)bbox.get("y2"));
    }

    @Override
    public DBObject query(Envelope bbox, MongoDataset data) {
        Polygon p = Envelopes.toPolygon(bbox);
        return BasicDBObjectBuilder.start().push(mapping.geometry().join()).push("$geoIntersects")
            .append("$geometry", GeoJSON.toObject(p)).get();
    }
}
