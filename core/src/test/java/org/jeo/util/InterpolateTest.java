package org.jeo.util;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

public class InterpolateTest {

    @Test
    public void testLinear() {
        List<Integer> vals = Interpolate.linear(0, 10, 5);
        assertSequence(vals, 0,2,4,6,8,10);
    }

    @Test
    public void testExp() {
        List<Double> vals = Interpolate.exp(0d, 10d, 5);
        assertSequence(vals, 0.0, 0.62, 1.61, 3.22, 5.81, 10.0);
    }

    @Test
    public void testLog() {
        List<Double> vals = Interpolate.log(0d, 10d, 5);
        assertSequence(vals, 0.0, 2.63, 4.85, 6.78, 8.48, 10.0);
    }

    void assertSequence(List<? extends Number> vals, Number... seq) {
        assertEquals(seq.length, vals.size());

        for (int i = 0; i < vals.size(); i++) {
            assertEquals(vals.get(i).doubleValue(), seq[i].doubleValue(), 0.01);
        }
    }
}
