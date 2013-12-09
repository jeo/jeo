package org.jeo.nano;

import org.junit.Test;
import org.jeo.nano.NanoHTTPD.Response;
import org.junit.Before;

public class DataHandlerTest extends HandlerTestSupport {

    @Before
    public void init() {
        handler = new DataHandler();
    }

    @Test
    public void testPattern() {
        testWorkspaceDataPattern(DataHandler.DATA_URI_RE, "/data", true, false);
    }

    @Test
    public void testWorkspace() throws Exception {
        mock = MockServer.create().
                withSingleDataWorkspace().
                replay();

        Response resp = makeRequest(
                new Request("/data/foo/", "GET", null, q(), null),
                NanoHTTPD.HTTP_OK,
                NanoHTTPD.MIME_JSON
        );

        assertJSONEquals(resp, "{'type':'workspace','driver':'mockDriver','datasets':['mockDataSet']}");

        mock.verify();
    }

    @Test
    public void testWorkspaceDataset() throws Exception {
        mock = MockServer.create().
                withVectorLayer().
                withMoreDetails().
                replay();

        Response resp = makeRequest(
                new Request("/data/foo/bar", "GET", null, q(), null),
                NanoHTTPD.HTTP_OK,
                NanoHTTPD.MIME_JSON
        );

        assertJSONEquals(resp, "{'name':'emptylayer','type':'vector',"
                + "'driver':'mockDriver','bbox':[-180.0,-90.0,180.0,90.0],"
                + "'crs':['+proj=longlat','+datum=WGS84','+no_defs'],"
                + "'count':42,'schema':{'name':'String'},"
                + "'features':'\\/features\\/foo\\/bar.json'}");

        mock.verify();
    }

    @Test
    public void testBadRequests() throws Exception {
        mock = MockServer.create().
                withVectorLayer().
                withMoreDetails().
                replay();

        makeBadRequest(
                new Request("/data/baz", "GET", null, q(), null),
                NanoHTTPD.HTTP_NOTFOUND,
                "No such workspace: baz"
        );

        makeBadRequest(
                new Request("/data/foo/baz", "GET", null, q(), null),
                NanoHTTPD.HTTP_NOTFOUND,
                "no such dataset: baz in workspace: foo"
        );
    }
}
