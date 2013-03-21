package org.jeo.mongo;

import java.io.ByteArrayInputStream;

import org.bson.BSONObject;
import org.jeo.feature.Feature;
import org.jeo.geojson.GeoJSON;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;

public class GeoJSONMapper implements MongoMapper {

    @Override
    public Feature feature(DBObject obj) {
        try {
            return (Feature) GeoJSON.read(new ByteArrayInputStream(JSON.serialize(obj).getBytes()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void feature(DBObject obj, Feature f) {
        obj.putAll((BSONObject) JSON.parse(GeoJSON.toString(f)));
    }

}
