package org.jeo.data;

import org.jeo.util.Key;
import org.junit.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DriversTest {

    @Test
    public void testParseURI() throws Exception {
        List<Key> keys = new ArrayList<>();
        keys.add(new Key("foo", String.class));
        keys.add(new Key("bar", Integer.class));

        Driver d = createMock(Driver.class);
        expect(d.keys()).andReturn(keys).anyTimes();
        replay(d);

        Map<String,Object> opts = Drivers.parseURI(new URI("x://?"), d);
        assertTrue(opts.isEmpty());

        opts = Drivers.parseURI(new URI("x://blah"), d);
        assertEquals(1, opts.size());
        assertEquals("blah", opts.get("foo"));

        opts = Drivers.parseURI(new URI("x://?foo=blah&bar=bam!&x=y"), d);
        assertEquals(3, opts.size());
        assertEquals("blah", opts.get("foo"));
        assertEquals("bam!", opts.get("bar"));
        assertEquals("y", opts.get("x"));
    }
}
