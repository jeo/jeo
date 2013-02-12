package org.jeo.nano;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.jeo.data.Registry;
import org.jeo.data.Tile;
import org.jeo.data.TileSet;
import org.jeo.data.Workspace;
import org.jeo.nano.NanoHTTPD.Response;
import org.junit.Test;

public class TileHandlerTest extends HandlerTestSupport {

    @Test
    public void testGet() throws Exception {
        TileSet layer = createMock(TileSet.class);
        expect(layer.read(1, 2, 3)).andReturn(new Tile(1,2,3,new byte[]{},"image/png")).once();
        replay(layer);

        Workspace ws = createMock(Workspace.class);
        expect(ws.get("bar")).andReturn(layer).once();
        replay(ws);

        Registry reg = createMock(Registry.class);
        expect(reg.get("foo")).andReturn(ws).once();
        replay(reg);

        Request req = new Request("/tiles/foo/bar/1/2/3.png", "GET", null, null, null);
        TileHandler h = new TileHandler(reg);
        assertTrue(h.canHandle(req));

        Response res = h.handle(req);
        assertEquals(NanoHTTPD.HTTP_OK, res.status);
        assertEquals("image/png", res.mimeType);

        verify(layer, ws, reg);
    }
}
