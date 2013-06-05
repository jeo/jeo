package org.jeo.mongo;

public class MongoGeoJSONTest extends MongoTest {

    @Override
    protected MongoTestData createTestData() {
        return new GeoJSONTestData();
    }
}
