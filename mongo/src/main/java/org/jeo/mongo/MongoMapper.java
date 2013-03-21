package org.jeo.mongo;

import org.jeo.feature.Feature;

import com.mongodb.DBObject;

public interface MongoMapper {

    Feature feature(DBObject obj);

    void feature(DBObject obj, Feature f);
}
