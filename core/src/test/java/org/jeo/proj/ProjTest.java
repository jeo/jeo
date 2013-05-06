package org.jeo.proj;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.jeo.geom.GeometryBuilder;
import org.jeo.proj.wkt.ProjWKTParser;
import org.junit.Test;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;

public class ProjTest {

    @Test
    public void testCrs() {
        CoordinateReferenceSystem crs = Proj.crs("EPSG:4326");
        assertNotNull(crs);
    }

    @Test
    public void testEpsgCode() {
        CoordinateReferenceSystem crs = Proj.crs("EPSG:4326");
        assertEquals(4326, Proj.epsgCode(crs).intValue());
    }

    @Test
    public void testBounds() {
        CoordinateReferenceSystem crs = Proj.crs("EPSG:3005");
        assertNotNull(Proj.bounds(crs));
    }

    @Test
    public void testReproject() throws Exception {
        GeometryBuilder gb = new GeometryBuilder();

        Point p = Proj.reproject(gb.point(-117, 63.15), Proj.crs("epsg:4326"), Proj.crs("epsg:3157"));
        assertEquals(802027.258082, p.getX(), 0.1);
        assertEquals(7016429.376474, p.getY(), 0.1);
    }

    @Test
    public void testReprojectEnvelope() throws Exception {
        Envelope e = new Envelope(490967.4065, 491909.5552, 5457747.3926, 5458839.7600);
        e = Proj.reproject(e, Proj.crs("epsg:3157"), Proj.crs("epsg:4326"));
        assertEquals(-123.12, e.getMinX(), 0.01);
        assertEquals(-123.11, e.getMaxX(), 0.01);
        assertEquals(49.27, e.getMinY(), 0.01);
        assertEquals(49.28, e.getMaxY(), 0.01);
    }

    @Test
    public void test900913() {
        assertNotNull(Proj.crs("epsg:900913"));
    }

    @Test
    public void testParseWKT() throws Exception {
        String wkt = dq("GEOGCS['GCS_WGS_1984'," +
                        "DATUM['WGS_1984', " +
                            "SPHEROID['WGS_1984',6378137,298.257223563]]," +
                        "PRIMEM['Greenwich',0]," +
                        "UNIT['Degree',0.017453292519943295]]");

        CoordinateReferenceSystem crs1 = Proj.fromWKT(wkt);
        CoordinateReferenceSystem crs2 = Proj.crs("epsg:4326");
        CoordinateReferenceSystem crs3 = Proj.crs("epsg:3157");

        Point p1 = new GeometryBuilder().point(-117, 63.15);
        Point p2 = new GeometryBuilder().point(-117, 63.15);

        p1 = Proj.reproject(p1, crs1, crs3);
        p2 = Proj.reproject(p2, crs2, crs3);
        assertTrue(p1.equals(p2));
    }

    @Test
    public void testParseProjWKT() throws Exception {
        String wkt = dq(
            "PROJCS['NAD83 / UTM zone 10N', "+
                    "  GEOGCS['NAD83', "+
                    "    DATUM['North American Datum 1983', "+
                    "      SPHEROID['GRS 1980', 6378137.0, 298.257222101, AUTHORITY['EPSG','7019']], "+
                    "      TOWGS84[0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0], "+
                    "      AUTHORITY['EPSG','6269']], "+
                    "    PRIMEM['Greenwich', 0.0, AUTHORITY['EPSG','8901']], "+
                    "    UNIT['degree', 0.017453292519943295], "+
                    "    AXIS['Geodetic longitude', EAST], "+
                    "    AXIS['Geodetic latitude', NORTH], "+
                    "    AUTHORITY['EPSG','4269']], "+
                    "  PROJECTION['Transverse_Mercator', AUTHORITY['EPSG','9807']], "+
                    "  PARAMETER['central_meridian', -123.0], "+
                    "  PARAMETER['latitude_of_origin', 0.0], "+
                    "  PARAMETER['scale_factor', 0.9996], "+
                    "  PARAMETER['false_easting', 500000.0], "+
                    "  PARAMETER['false_northing', 0.0], "+
                    "  UNIT['m', 1.0], "+
                    "  AXIS['Easting', EAST], "+
                    "  AXIS['Northing', NORTH], "+
                    "  AUTHORITY['EPSG','26910']]");

        CoordinateReferenceSystem geo = Proj.EPSG_4326;
        CoordinateReferenceSystem crs1 = Proj.fromWKT(wkt);
        CoordinateReferenceSystem crs2 = Proj.crs("EPSG:26910");

        Point p1 = new GeometryBuilder().point(-117, 63.15);
        Point p2 = new GeometryBuilder().point(-117, 63.15);

        p1 = Proj.reproject(p1, geo, crs1);
        p2 = Proj.reproject(p2, geo, crs2);
        assertTrue(p1.equals(p2));
    }

    @Test
    public void testEncodeWKT() throws Exception {
        CoordinateReferenceSystem crs = Proj.crs("epsg:4326");
    }

    String dq(String str) {
        return str.replaceAll("'", "\"");
    }
    
}
