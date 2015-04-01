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

import io.jeo.TestData;
import io.jeo.vector.VectorQuery;
import io.jeo.vector.VectorDataset;
import io.jeo.vector.Feature;
import io.jeo.vector.Schema;
import io.jeo.geojson.GeoJSONWriter;
import io.jeo.geom.Geom;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;

public class MongoTestData {

    public void setUp(DBCollection dbcol, MongoWorkspace workspace) throws IOException {
        VectorDataset data = TestData.states();
        Schema schema = data.schema();

        for (Feature f : data.cursor(new VectorQuery())) {
            Geometry g = f.geometry();
            g = Geom.iterate((MultiPolygon) f.geometry()).iterator().next();

            DBObject obj = new BasicDBObject(f.map());
            obj.put(schema.geometry().name(), JSON.parse(GeoJSONWriter.toString(g)));
            dbcol.insert(obj);
        }

        dbcol.ensureIndex(BasicDBObjectBuilder.start().add(
            data.schema().geometry().name(), "2dsphere").get());

        workspace.setMapper(new DefaultMapper(new Mapping().geometry("geometry")));
    }
}
