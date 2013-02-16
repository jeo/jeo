package org.jeo.gmaps;

import static org.junit.Assert.*;

import org.jeo.geom.GeometryBuilder;
import org.junit.Before;
import org.junit.Test;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class GeomeryAdapterTest {

    GeometryBuilder gb;
    GeometryAdapter ga;

    @Before
    public void setUp() {
        gb = new GeometryBuilder();
        ga = new GeometryAdapter();
    }

    @Test
    public void testPoint() {
        MarkerOptions opts = ga.adapt(gb.point(0,0));
        assertNotNull(opts);

        assertEquals(new LatLng(0,0), opts.getPosition());
    }

    @Test
    public void testLineString() {
        PolylineOptions opts = ga.adapt(gb.lineString(1,1, 2,2));
        assertNotNull(opts);

        assertEquals(2, opts.getPoints().size());
        assertEquals(new LatLng(1,1), opts.getPoints().get(0));
        assertEquals(new LatLng(2,2), opts.getPoints().get(1));
    }

    @Test
    public void testPolygon() {
        PolygonOptions opts = ga.adapt(gb.polygon(1,1,2,2,3,3,1,1));
        assertNotNull(opts);

        assertEquals(4, opts.getPoints().size());
        assertEquals(new LatLng(1,1), opts.getPoints().get(0));
        assertEquals(new LatLng(2,2), opts.getPoints().get(1));
        assertEquals(new LatLng(3,3), opts.getPoints().get(2));
        assertEquals(new LatLng(1,1), opts.getPoints().get(3));
    }
}
