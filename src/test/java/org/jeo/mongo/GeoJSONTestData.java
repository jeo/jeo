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
package org.jeo.mongo;

import java.io.IOException;

import org.jeo.TestData;
import org.jeo.data.Query;
import org.jeo.data.VectorDataset;
import org.jeo.feature.Feature;
import org.jeo.geojson.GeoJSONWriter;
import org.jeo.geom.Geom;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.vividsolutions.jts.geom.MultiPolygon;

public class GeoJSONTestData extends MongoTestData {

    public void setUp(DBCollection dbcol, MongoWorkspace workspace) throws IOException {
        VectorDataset data = TestData.states();
        for (Feature f : data.cursor(new Query())) {
            f.put("geometry", Geom.iterate((MultiPolygon) f.geometry()).iterator().next());
            dbcol.insert((DBObject) JSON.parse(GeoJSONWriter.toString(f)));
        }
    
        dbcol.ensureIndex(BasicDBObjectBuilder.start().add("geometry", "2dsphere").get());
        workspace.setMapper(new GeoJSONMapper());
    }
}
