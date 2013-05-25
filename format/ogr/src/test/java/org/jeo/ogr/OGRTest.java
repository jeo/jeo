package org.jeo.ogr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.jeo.Tests;
import org.jeo.data.Query;
import org.jeo.data.VectorData;
import org.jeo.feature.Feature;
import org.jeo.shp.ShpData;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public class OGRTest {

    @Before
    public void setUp() {
        Exception err = null;
        try {
            System.loadLibrary("gdaljni");
        }
        catch(Exception e) {
            err = e;
        }

        Assume.assumeNoException(err);
    }

    @Test
    public void testReadGML() throws IOException {
        File tmp = Tests.newTmpDir(); 
        Tests.unzip(getClass().getResourceAsStream("states_gml2.zip"), tmp);

        File gml = new File(tmp, "states_gml2.gml");
        VectorData data = new GML().open((Map) Collections.singletonMap(GML.PATH, gml.getPath()));
        assertNotNull(data);

        assertNotNull(data.getCRS());
        assertEquals(49, data.count(new Query()));
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
}
