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

import io.jeo.vector.Feature;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import io.jeo.vector.FeatureCursor;

public class MongoCursor extends FeatureCursor {

    DBCursor dbCursor;
    MongoMapper mapper;
    MongoDataset dataset;

    Feature next;

    MongoCursor(Mode mode, DBCursor dbCursor, MongoDataset dataset) {
        super(mode);
        this.dbCursor = dbCursor;
        this.dataset = dataset;
        this.mapper = dataset.mapper();
    }

    @Override
    public boolean hasNext() throws IOException {
        return mode == APPEND || dbCursor.hasNext();
    }

    @Override
    public Feature next() throws IOException {
        return next = mapper.feature(mode==APPEND ? new BasicDBObject() : dbCursor.next(), dataset);
    }

    @Override
    protected void doRemove() throws IOException {
        dbCursor.getCollection().remove(mapper.object(next, dataset));
    }

    @Override
    protected void doWrite() throws IOException {
        //TODO: check write result
        if (getMode() == APPEND) {
            DBObject obj = mapper.object(next, dataset);
            dataset.getCollection().insert(obj);
        }
        else {
            DBObject obj = mapper.object(next, dataset);
            dataset.getCollection().update(new BasicDBObject("_id", obj.get("_id")), obj);
        }
    }

    @Override
    public void close() throws IOException {
        if (dbCursor != null) {
            dbCursor.close();
            dbCursor = null;
        }
    }

    
}
