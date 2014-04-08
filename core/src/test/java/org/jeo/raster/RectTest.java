package org.jeo.raster;


import com.vividsolutions.jts.geom.Envelope;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RectTest {

    @Test
    public void testMap() {
        Rect r = new Rect(0, 0, 1024, 512);
        Envelope world = new Envelope(-180,180,-90,90);

        Rect s = r.map(new Envelope(-45,45,-45,45), world);
        assertEquals(384d, s.left, 0.1);
        assertEquals(128d, s.top, 0.1);
        assertEquals(640d, s.right, 0.1);
        assertEquals(384d, s.bottom, 0.1);
    }
}
