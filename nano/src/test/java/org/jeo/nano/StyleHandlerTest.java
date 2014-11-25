package org.jeo.nano;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import org.easymock.IAnswer;
import org.jeo.data.DataRepositoryView;
import org.jeo.data.Handle;
import org.jeo.filter.Filter;
import org.jeo.geojson.GeoJSONReader;
import org.jeo.json.JSONObject;
import org.jeo.json.JSONValue;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Justin Deoliveira, Boundless
 */
public class StyleHandlerTest {

    MockServer mock;
    StyleHandler handler;

    @Before
    public void setUp() throws Exception {
        mock = MockServer.create();
        mock.createStyle("foo");
        mock.buildRegistry().replay();

        handler = new StyleHandler();
    }

    @Test
    public void testCanHandle() {
        handler.canHandle(new Request("/styles", "GET"), mock.server);
        handler.canHandle(new Request("/styles/foo", "GET"), mock.server);
    }

    @Test
    public void testGet() throws Exception {
        Request req = new Request("/styles/foo", "GET");
        assertTrue(handler.canHandle(req, mock.server));

        NanoHTTPD.Response r = handler.handle(req, mock.server);
        assertEquals("text/css", r.mimeType);
    }

    @Test
    public void testGetAll() throws Exception {
        Request req = new Request("/styles", "GET");
        assertTrue(handler.canHandle(req, mock.server));

        NanoHTTPD.Response r = handler.handle(req, mock.server);

        JSONObject obj = (JSONObject) JSONValue.parseWithException(new InputStreamReader(r.stream()));
        assertTrue(obj.keySet().contains("foo"));
    }
}
