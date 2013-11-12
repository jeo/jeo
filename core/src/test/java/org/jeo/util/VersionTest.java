package org.jeo.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class VersionTest {

    @Test
    public void testCompare() {
        Version v = new Version(1,2,3);
        assertEquals(1, v.compareTo(new Version(0,2,3)));
        assertEquals(1, v.compareTo(new Version(1,0,3)));
        assertEquals(1, v.compareTo(new Version(1,2,0)));

        assertEquals(-1, v.compareTo(new Version(2,2,3)));
        assertEquals(-1, v.compareTo(new Version(1,3,3)));
        assertEquals(-1, v.compareTo(new Version(1,2,4)));

        assertEquals(0, v.compareTo(new Version(1,2,3)));
    }

    @Test
    public void testEquals() {
        assertEquals(new Version(1,2,3), new Version(1,2,3));
    }

    @Test
    public void testParse() {
        assertEquals(new Version(1,2,3), new Version("1.2.3"));
        assertEquals(new Version(1,2,0), new Version("1.2"));
        assertEquals(new Version(1,0,0), new Version("1"));
    }
}
