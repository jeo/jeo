package org.jeo.nano;

import java.io.IOException;
import org.jeo.data.Tile;
import org.jeo.data.TileDataset;
import org.jeo.nano.NanoHTTPD.Response;
import org.junit.Before;
import org.junit.Test;

public class TileHandlerTest extends HandlerTestSupport {

    @Before
    public void init() {
        handler = new TileHandler();
    }

    @Test
    public void testPattern() {
        assertPattern(TileHandler.TILES_URI_RE, "/tiles/work-space/dataset", "work-space", "dataset");
        assertPattern(TileHandler.TILES_URI_RE, "/tiles/workspace/data-set.html", "workspace", "data-set", null, null, null, "html");
        assertPattern(TileHandler.TILES_URI_RE, "/tiles/work_space/data_set/1/2/3.jpg", "work_space", "data_set", "1", "2", "3", "jpg");
    }

    @Test
    public void testGet() throws Exception {
        mock = MockServer.create().
                withTileLayer(true).
                replay();

        makeRequest(
                new Request("/tiles/foo/bar/1/2/3.png", "GET", null, null, null),
                NanoHTTPD.HTTP_OK,
                "image/png"
        );

        mock.verify();
    }

    @Test
    public void testGetHTML() throws Exception {
        mock = MockServer.create().
                withTileLayer(false).
                withMoreDetails().
                replay();

        handler = new TileHandler() {

            @Override
            Tile getFirstTile(TileDataset layer) throws IOException {
                return new Tile(1, 2, 3);
            }

        };

        Response res = makeRequest(
                new Request("/tiles/foo/bar.html", "GET", null, null, null),
                NanoHTTPD.HTTP_OK,
                NanoHTTPD.MIME_HTML
        );

        String body = read(res);
        assertContains(body, "new OpenLayers.Bounds(-42.000000,-42.000000,42.000000,42.000000)");

        mock.verify();
    }
}
