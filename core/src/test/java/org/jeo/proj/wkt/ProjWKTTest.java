package org.jeo.proj.wkt;

import org.jeo.proj.Proj;
import org.junit.Test;
import org.osgeo.proj4j.CoordinateReferenceSystem;

public class ProjWKTTest {
    
    @Test
    public void testGeographic() throws Exception {
        CoordinateReferenceSystem crs = Proj.crs("epsg:4326");
        new ProjWKTEncoder().encode(crs, false);
    }

    @Test
    public void testProjected() throws Exception {
        CoordinateReferenceSystem crs = Proj.crs("epsg:3157");
        new ProjWKTEncoder().encode(crs, false);
    }
}
