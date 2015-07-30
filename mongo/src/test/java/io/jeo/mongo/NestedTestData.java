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
import io.jeo.geom.Geom;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;

public class NestedTestData extends MongoTestData {

    @Override
    public void setUp(DBCollection dbcol, MongoWorkspace workspace) throws IOException {
        VectorDataset data = TestData.states();
        
        for (Feature f : data.read(new VectorQuery())) {
            Geometry g = f.geometry();
            g = Geom.iterate((MultiPolygon) f.geometry()).iterator().next();

            DBObject obj = new BasicDBObject();

            DBObject geo = new BasicDBObject();
            geo.put("shape", GeoJSON.toObject(g));
            geo.put("center", GeoJSON.toObject(g.getCentroid()));
            obj.put("geo", geo);
            
            obj.put("STATE_NAME", f.get("STATE_NAME"));
            obj.put("STATE_ABBR", f.get("STATE_ABBR"));

            DBObject pop = new BasicDBObject();
            pop.put("total", f.get("SAMP_POP"));
            pop.put("male", f.get("P_MALE"));
            pop.put("female", f.get("P_FEMALE"));
            obj.put("pop", pop);

            dbcol.insert(obj);
        }

        dbcol.ensureIndex(BasicDBObjectBuilder.start().add("geo.shape", "2dsphere").get());

        Mapping mapping = new Mapping().geometry("geo.shape").geometry("geo.center");
        workspace.setMapper(new DefaultMapper(mapping));
    }
}
