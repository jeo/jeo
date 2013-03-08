package org.jeo.mongo;

import java.io.IOException;

import org.jeo.geojson.GeoJSON;
import org.jeo.geom.GeometryBuilder;
import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.vividsolutions.jts.geom.Polygon;

public class MongoTest {

    @Test
    public void test() throws IOException {

        Polygon p = (Polygon) new GeometryBuilder().point(0,0).buffer(1);

        BasicDBObjectBuilder b = new BasicDBObjectBuilder();
        DBObject obj = b.start().push("loc").append("$geoIntersets", JSON.parse(GeoJSON.toString(p))).get();
        System.out.println(obj);

    }
}
