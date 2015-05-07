package io.jeo.geom;

import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GeomTest {

    @Test
    public void testNarrow() {
        GeometryCollection gcol = Geom.build()
            .point(1, 1).point()
            .point(2, 2).point()
            .point(3, 3).point()
            .toCollection();

        assertTrue(Geom.narrow(gcol) instanceof MultiPoint);
    }

    @Test
    public void testFlatten() {
        GeometryCollection gcol = Geom.build()
            .point(1,1).point()
            .point(2,3).point(4,5).multiPoint()
            .toCollection();

        List<Point> flat = Geom.flatten(gcol);
        assertEquals(3, flat.size());
        assertEquals(1d, flat.get(0).getX(), 0.1);
        assertEquals(2d, flat.get(1).getX(), 0.1);
        assertEquals(4d, flat.get(2).getX(), 0.1);
    }
}
