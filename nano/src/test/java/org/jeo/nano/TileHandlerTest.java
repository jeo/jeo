package org.jeo.nano;

import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.jeo.data.DataRepository;
import org.jeo.data.Tile;
import org.jeo.data.TileDataset;
import org.jeo.data.Workspace;
import org.jeo.nano.NanoHTTPD.Response;
import org.junit.Test;

public class TileHandlerTest extends HandlerTestSupport {

    @Test
    public void testGet() throws Exception {
        TileDataset layer = createMock(TileDataset.class);
        expect(layer.read(1, 2, 3)).andReturn(new Tile(1,2,3,new byte[]{},"image/png")).once();
        layer.close();
        expectLastCall().once();
        replay(layer);

        Workspace ws = createMock(Workspace.class);
        expect(ws.get("bar")).andReturn(layer).once();
        ws.close();
        expectLastCall().once();
        replay(ws);

        DataRepository reg = createMock(DataRepository.class);
        expect(reg.get("foo")).andReturn(ws).once();
        replay(reg);

        NanoServer server = createMock(NanoServer.class);
        expect(server.getRegistry()).andReturn(reg).anyTimes();
        replay(server);

        Request req = new Request("/tiles/foo/bar/1/2/3.png", "GET", null, null, null);
        TileHandler h = new TileHandler();
        assertTrue(h.canHandle(req, server));

        Response res = h.handle(req, server);
        assertEquals(NanoHTTPD.HTTP_OK, res.status);
        assertEquals("image/png", res.mimeType);

        verify(layer, ws, reg);
    }
}
