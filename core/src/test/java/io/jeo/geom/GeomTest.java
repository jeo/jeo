package io.jeo.geom;

import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.MultiPoint;
import org.junit.Test;

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
}
