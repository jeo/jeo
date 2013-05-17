package org.jeo.geojson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jeo.Tests;
import org.jeo.data.Cursor;
import org.jeo.data.Query;
import org.jeo.feature.Feature;
import org.jeo.feature.Features;
import org.jeo.shp.ShpData;
import org.jeo.shp.ShpDataset;
import org.junit.Before;
import org.junit.Test;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.vividsolutions.jts.geom.Envelope;

public class GeoJSONTest {

    GeoJSONDataset data;

    @Before
    public void setUp() throws Exception {
        File dir = Tests.unzip(getClass().getResourceAsStream("states.zip"), Tests.newTmpDir());
        data = new GeoJSONDataset(new File(dir, "states.json"));
    }

    @Test
    public void testCRS() throws Exception {
        CoordinateReferenceSystem crs = data.getCRS();
        assertNotNull(crs);
        assertEquals("EPSG:4326", crs.getName());
    }

    @Test
    public void testBounds() throws Exception {
        Envelope bbox = data.bounds();
        assertNotNull(bbox);

        assertEquals(-124.73, bbox.getMinX(), 0.01);
        assertEquals(24.96, bbox.getMinY(), 0.01);
        assertEquals(-66.97, bbox.getMaxX(), 0.01);
        assertEquals(49.37, bbox.getMaxY(), 0.01);
    }

    @Test
    public void testCount() throws Exception {
        assertEquals(49, data.count(new Query()));
    }

    @Test
    public void testRead() throws Exception {
        Set<String> names = Sets.newHashSet(Iterables.transform(ShpData.states().cursor(new Query()), 
            new Function<Feature, String>() {
                @Override
                public String apply(Feature input) {
                    return (String) input.get("STATE_NAME");
                }
            }));

        assertEquals(49, names.size());
        for (Feature f : data.cursor(new Query())) {
            names.remove(f.get("STATE_NAME"));
        }

        assertTrue(names.isEmpty());
    }

    @Test
    public void testAppend() throws Exception {
        ShpDataset shp = ShpData.states();

        Map map = new HashMap();
        map.put(GeoJSON.FILE, Tests.newTmpFile());

        GeoJSONDataset json = new GeoJSON().create(map, shp.getSchema());

        Cursor<Feature> from = shp.cursor(new Query());
        Cursor<Feature> to = json.cursor(new Query().append());
        
        for (Feature f : from) {
            Feature g = to.next();
            Features.copy(f, g);
            to.write();
        }

        to.close();

        assertEquals(49, json.count(new Query()));
    }
}
