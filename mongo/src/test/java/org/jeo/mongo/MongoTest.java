package org.jeo.mongo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jeo.data.Cursor;
import org.jeo.feature.Feature;
import org.jeo.feature.Features;
import org.jeo.feature.Schema;
import org.jeo.geojson.GeoJSON;
import org.jeo.geom.Geom;
import org.jeo.geom.GeometryBuilder;
import org.jeo.shp.Shapefile;
import org.jeo.shp.ShpData;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.util.JSON;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;

public class MongoTest {

    static DB db;

    @BeforeClass
    public static void connect() {
        Exception error = null;
        try {
            Mongo mongo = new Mongo();
            db = mongo.getDB("jeo");
            db.getCollectionNames();
        } catch (Exception e) {
            error = e;
        }

        Assume.assumeNoException(error);
        Assume.assumeNotNull(db);
    }

    MongoDB mongo;

    @Before
    public void setUp() throws Exception {
        DBCollection states = db.getCollection("states");
        if (states != null) {
            states.remove(new BasicDBObject());
        }

        Shapefile shp = ShpData.states();
        for (Feature f : shp.read(null)) {
            f.put("geometry", Geom.iterate((MultiPolygon) f.geometry()).iterator().next());
            states.insert((DBObject) JSON.parse(GeoJSON.toString(f)));
        }

        states.ensureIndex(BasicDBObjectBuilder.start().add("geometry", "2dsphere").get());
        mongo = new MongoDB(db);
    }

    @After
    public void tearDown() throws Exception {
        mongo.dispose();
    }

    @Test
    public void testGet() throws Exception {
        MongoDataset states = mongo.get("states");
        assertNotNull(states);

        Schema schema = states.getSchema();
        assertNotNull(schema);
    }

    @Test
    public void testCount() throws Exception {
        MongoDataset states = mongo.get("states");
        assertEquals(49, states.count(null));
    }

    @Test
    public void testCountBBOX() throws Exception {
        MongoDataset states = mongo.get("states");
        Envelope bbox = new Envelope(-73.44, -71.51, 42.73, 45.01);

        int match = Iterables.size(filter(states.read(null), bbox));
        assertEquals(match, states.count(bbox));
    }

    @Test
    public void testReadAll() throws Exception {
        MongoDataset states = mongo.get("states");
        int count = 0;
        for (Feature f : states.read(null)) {
            count++;
            assertNotNull(f.geometry());
            assertNotNull(f.get("STATE_NAME"));
        }

        assertEquals(49, count);
    }

    @Test
    public void testReadBBOX() throws Exception {
        MongoDataset states = mongo.get("states");
        Envelope bbox = new Envelope(-73.44, -71.51, 42.73, 45.01);

        Set<String> names = Sets.newHashSet(Iterables.transform(filter(states.read(null),bbox), 
            new Function<Feature,String>() {
                @Override
                public String apply(Feature input) {
                    return (String) input.get("STATE_NAME");
                }
            }));
        assertFalse(names.isEmpty());

        for (Feature f : states.read(bbox)) {
            names.remove(f.get("STATE_NAME"));
        }
        assertTrue(names.isEmpty());
    }

    @Test
    public void testAdd() throws Exception {
        MongoDataset states = mongo.get("states");
        Map<String,Object> map = new HashMap<String,Object>();

        Geometry g = new GeometryBuilder().point(0,0).buffer(1);
        map.put("geometry", g);
        map.put("STATE_NAME", "Nowhere");

        states.add(Features.create(map));
        assertEquals(50, states.count(null));

        
        Cursor<Feature> c = states.read(g.getEnvelopeInternal());
        assertTrue(c.hasNext());
        Feature f = c.next();

        assertTrue(g.equals(f.geometry()));
        assertEquals("Nowhere", f.get("STATE_NAME"));
    }

    Iterable<Feature> filter(Cursor<Feature> features, final Envelope bbox) {
        return Iterables.filter(features, new Predicate<Feature>() {
            @Override
            public boolean apply(Feature input) {
                return input.geometry().getEnvelopeInternal().intersects(bbox);
            }
        }); 
    }

    
}
