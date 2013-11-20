package org.jeo.nano;

import static org.easymock.classextension.EasyMock.createMock;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.jeo.data.Cursor;
import org.jeo.feature.Feature;
import org.jeo.geojson.GeoJSONReader;
import org.jeo.nano.NanoHTTPD.Response;
import org.junit.Before;
import org.junit.Test;


public class FeatureHandlerTest extends HandlerTestSupport {

    @Before
    public void init() {
        handler = new FeatureHandler();
    }

    @Test
    public void testPattern() {
        testWorkspaceDataPattern(FeatureHandler.FEATURES_URI_RE, "/features", false, true);
    }

    @Test
    public void testGetWorkspaceDatasetJSON() throws Exception {
        mock = MockServer.create()
                .withVectorLayer()
                    .withNoFeatures()
                .replay();

        Response res = makeRequest(
                new Request("/features/foo/bar", "GET", null, q("bbox","-180,-90,180,90"), null),
                NanoHTTPD.HTTP_OK,
                NanoHTTPD.MIME_JSON
        );
        
        Cursor<Feature> features = (Cursor<Feature>) new GeoJSONReader().read(res.data);
        assertFalse(features.hasNext());

        mock.verify();
    }

    @Test
    public void testGetWorkspaceDatasetHTML() throws Exception {
        mock = MockServer.create()
                .withVectorLayer()
                    .withMoreDetails()
                .replay();

        Response res = makeRequest(
            new Request("/features/foo/bar.html", "GET", null, q(), null),
            NanoHTTPD.HTTP_OK,
            NanoHTTPD.MIME_HTML
        );

        String body = read(res);
        assertContains(body, "url: '/features/foo/bar'");

        mock.verify();
    }

    @Test
    public void testGetWorkspaceDatasetPNG() throws Exception {
        mock = MockServer.create()
                .withVectorLayer()
                    .withPointGeometry()
                .replay();

        MapRenderer renderer = createMock(MapRenderer.class);
        handler = new FeatureHandler(renderer);

        makeRequest(
                new Request("/features/foo/bar.png", "GET", null, q(), null),
                NanoHTTPD.HTTP_OK,
                NanoHTTPD.MIME_PNG
        );

        mock.verify();
    }

    @Test
    public void testPostAddFeatures() throws Exception {
        mock = MockServer.create()
                    .withVectorLayer()
                        .withSingleFeature()
                .replay();

        String json = dequote("{'type':'Feature','geometry':{'type':'Point','coordinates':[0.0,0.0]}," +
            "'properties':{'name':'zero'}}");
        makeRequest(
                new Request("/features/foo/bar", "POST", h("Content-type", "application/json"), null, body(json)),
                NanoHTTPD.HTTP_CREATED,
                NanoHTTPD.MIME_PLAINTEXT
        );

        mock.verify();
    }

    @Test
    public void testPostCreateLayer() throws Exception {
        mock = MockServer.create()
                    .withWorkspace()
                    .expectSchemaCreated()
                .replay();

        String json = dequote("{ 'type': 'schema','name': 'widgets', 'properties': { 'geometry': { 'type': 'Point' }, " +
            "'name': { 'type': 'string' } } }");

        makeRequest(
                new Request("/features/foo", "POST", h("Content-type", "application/json"), null, body(json)),
                NanoHTTPD.HTTP_CREATED,
                NanoHTTPD.MIME_PLAINTEXT
        );

        mock.verify();
    }

    @Test
    public void testBadRequests() throws Exception {
        mock = MockServer.create()
                    .withVectorLayer()
                .replay();

        makeBadRequest(
                new Request("/features/baz", "GET", null, q(), null),
                NanoHTTPD.HTTP_NOTFOUND,
                "No such dataset at: /features/baz"
        );

        makeBadRequest(
                new Request("/features/foo/baz", "GET", null, q(), null),
                NanoHTTPD.HTTP_NOTFOUND,
                "no such dataset: baz in workspace: foo"
        );

        assertInvalidSRSResponse("1234", "Cannot locate provided srs: 1234");
        assertInvalidSRSResponse("xyz:1234", "Cannot locate provided srs: xyz:1234");
        assertInvalidSRSResponse("epsg:9999", "Cannot locate provided authority: epsg:9999");
        
    }

    private void assertInvalidSRSResponse(String srs, String message) throws Exception {
        mock = MockServer.create()
                    .withVectorLayer()
                .replay();
        makeBadRequest(
                new Request("/features/foo/bar", "GET", null, q("srs", srs), null),
                NanoHTTPD.HTTP_BADREQUEST,
                message
        );
    }

}
