package org.jeo.data;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

public class CachedRegistryTest {

    @Test
    public void testWorkspace() {
        Workspace ws = createMock(Workspace.class);

        Registry reg = createMock(Registry.class);
        expect(reg.get("foo")).andReturn(ws).once();

        replay(ws, reg);

        CachedRegistry cached = new CachedRegistry(reg);
        assertNotNull(cached.get("foo"));
        assertNotNull(cached.get("foo"));
        assertNotNull(cached.get("foo"));

        verify(ws, reg);
    }

    @Test
    public void testLayer() throws IOException {
        Dataset l = createMock(Dataset.class);
        
        Workspace ws = createMock(Workspace.class);
        expect(ws.get("bar")).andReturn(l).once();

        Registry reg = createMock(Registry.class);
        expect(reg.get("foo")).andReturn(ws).once();

        replay(l, ws, reg);

        CachedRegistry cached = new CachedRegistry(reg);
        assertNotNull(cached.get("foo").get("bar"));
        assertNotNull(cached.get("foo").get("bar"));
        assertNotNull(cached.get("foo").get("bar"));

        verify(l, ws, reg);
    }
}
