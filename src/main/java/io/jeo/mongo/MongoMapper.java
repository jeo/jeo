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

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.vividsolutions.jts.geom.Envelope;

/**
 * Strategy interface for mapping documents in a mongo collection to features.
 *  
 * @author Justin Deoliveira, OpenGeo
 */
public interface MongoMapper {

    /**
     * Creates a feature from a mongo object.
     */
    Feature feature(DBObject obj, MongoDataset data);

    /**
     * Creates a mongo object from a feature.
     */
    DBObject object(Feature f, MongoDataset data);

    /**
     * Computes the bounding box of a mongo collection. 
     */
    Envelope bbox(DBCollection dbcol, MongoDataset data);

    /**
     * Encodes a bounding box query.
     */
    DBObject query(Envelope bbox, MongoDataset data);
}
