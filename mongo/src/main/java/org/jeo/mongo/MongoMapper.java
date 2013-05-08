package org.jeo.mongo;

import org.jeo.feature.Feature;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.vividsolutions.jts.geom.Envelope;

public interface MongoMapper {

    Envelope bbox(DBCollection dbcol);

    Feature feature(DBObject obj);

    DBObject object(Feature f);

}
