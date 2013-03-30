package org.jeo.map;

import static org.junit.Assert.*;

import java.util.List;
import org.jeo.util.Interpolate.Method;

import org.junit.Test;

public class RGBTest {

    @Test
    public void testInterpolate() {
        List<RGB> vals = RGB.red.interpolate(RGB.blue, 10, Method.LINEAR);
        assertRGBs(vals, 255,0,0, 255,102,0, 255,204,0, 204,255,0, 102,255,0, 0,255,0, 0,255,102,
            0,255,204, 0,204,255, 0,102,255, 0,0,255);

    }

    void assertRGBs(List<RGB> rgbs, int... values) {
        assertEquals(values.length / 3, rgbs.size());
        for (int i = 0; i < values.length; i += 3) {
            RGB rgb = rgbs.get(i/3);
            assertEquals(rgb.getRed(), values[i]);
            assertEquals(rgb.getGreen(), values[i+1]);
            assertEquals(rgb.getBlue(), values[i+2]);
        }
    }
}
