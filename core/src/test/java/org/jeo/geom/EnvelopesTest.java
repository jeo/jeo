package org.jeo.geom;

import static org.junit.Assert.*;

import org.junit.Test;

import com.vividsolutions.jts.geom.Envelope;

public class EnvelopesTest {

    @Test
    public void testScale() {
        Envelope e = new Envelope(0,10,0,10);

        assertEnvelope(Envelopes.scale(e, 0.5), 2.5, 7.5, 2.5, 7.5);
        assertEnvelope(Envelopes.scale(e, 2), -5.0, 15.0, -5.0, 15.0);
        assertEnvelope(Envelopes.scale(e, 1), 0, 10, 0, 10);
    }

    @Test
    public void testTranslate() {
        Envelope e = new Envelope(0,10,0,10);
        assertEnvelope(Envelopes.translate(e, 5, 5), 5, 15, 5, 15);
        assertEnvelope(Envelopes.translate(e, 5, -5), 5, 15, -5, 5);
        assertEnvelope(Envelopes.translate(e, -5, 5), -5, 5, 5, 15);
    }

    void assertEnvelope(Envelope e, double x1, double x2, double y1, double y2) {
        assertEquals(x1, e.getMinX(), 1e-6);
        assertEquals(x2, e.getMaxX(), 1e-6);
        assertEquals(y1, e.getMinY(), 1e-6);
        assertEquals(y2, e.getMaxY(), 1e-6);
    }
}
