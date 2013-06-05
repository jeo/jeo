package org.jeo.mongo;

import org.jeo.feature.Feature;

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
