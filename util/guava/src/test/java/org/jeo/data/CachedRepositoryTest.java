package org.jeo.data;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

public class CachedRepositoryTest {

    @Test
    public void testWorkspace() throws IOException {
        Workspace ws = createMock(Workspace.class);

        DataRepository reg = createMock(DataRepository.class);
        expect(reg.get("foo", Workspace.class)).andReturn(ws).once();

        replay(ws, reg);

        CachedRepository cached = new CachedRepository(reg);
        assertNotNull(cached.get("foo", Workspace.class));
        assertNotNull(cached.get("foo", Workspace.class));
        assertNotNull(cached.get("foo", Workspace.class));

        verify(ws, reg);
    }

    @Test
    public void testLayer() throws IOException {
        Dataset l = createMock(Dataset.class);
        
        Workspace ws = createMock(Workspace.class);
        expect(ws.get("bar")).andReturn(l).once();

        DataRepository reg = createMock(DataRepository.class);
        expect(reg.get("foo", Workspace.class)).andReturn(ws).once();

        replay(l, ws, reg);

        CachedRepository cached = new CachedRepository(reg);
        assertNotNull(((Workspace)cached.get("foo", Workspace.class)).get("bar"));
        assertNotNull(((Workspace)cached.get("foo", Workspace.class)).get("bar"));
        assertNotNull(((Workspace)cached.get("foo", Workspace.class)).get("bar"));

        verify(l, ws, reg);
    }
}
