/* Copyright 2013 The jeo project. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jeo.nano;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import java.io.InputStreamReader;
import java.util.regex.Pattern;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.jeo.data.Cursor;
import org.jeo.vector.VectorQuery;
import org.jeo.vector.BasicFeature;
import org.jeo.vector.Feature;
import org.jeo.vector.Schema;
import org.jeo.vector.SchemaBuilder;
import org.jeo.geojson.GeoJSONReader;
import org.jeo.json.JSONArray;
import org.jeo.json.JSONObject;
import org.jeo.json.parser.JSONParser;
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
        Pattern p = FeatureHandler.FEATURES_URI_RE;
        assertFalse(p.matcher("/features").matches());
        assertFalse(p.matcher("/features/").matches());
        assertPattern(p, "/features/work-space/dataset", "work-space", "dataset");
        assertPattern(p, "/features/work-space/dataset/", "work-space", "dataset");
        assertPattern(p, "/features/work-space/dataset/fid", "work-space", "dataset", "fid");
        assertPattern(p, "/features/work_space/data-set.html", "work_space", "data-set", null, "html");
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
        
        Cursor<Feature> features = (Cursor<Feature>) new GeoJSONReader().read(res.stream());
        assertFalse(features.hasNext());

        mock.verify();
    }

    @Test
    public void testGetWorkspaceDatasetJSONAllFieldsWithQuery() throws Exception {
        // make sure all fields come back when using a filter
        GeometryFactory f = new GeometryFactory();
        mock = MockServer.create()
                .withVectorLayer()
                    .withFeature("22", "name","foo","value","bar","geom", f.createPoint(new Coordinate(0,0)))
                .replay();
        Response res = makeRequest(
                new Request("/features/foo/bar", "GET", null, q("filter","name=foo"), null),
                NanoHTTPD.HTTP_OK,
                NanoHTTPD.MIME_JSON
        );
        JSONObject parse = (JSONObject) new JSONParser().parse(new InputStreamReader(res.stream()));
        JSONArray features = (JSONArray) parse.get("features");
        JSONObject feature = (JSONObject) features.get(0);
        assertNotNull(feature.get("geometry"));
        assertEquals("22", feature.get("id"));
        JSONObject props = (JSONObject) feature.get("properties");
        assertEquals("foo", props.get("name"));
        assertEquals("bar", props.get("value"));
        mock.verify();
    }

    @Test
    public void testGetWorkspaceDatasetJSONSelectFieldsWithQuery() throws Exception {
        // make sure only specified fields come back when using a filter
        GeometryFactory f = new GeometryFactory();
        mock = MockServer.create()
                .withVectorLayer()
                    .withFeature("22", "name","foo","value","bar","geom", f.createPoint(new Coordinate(0,0)))
                .replay();
        Response res = makeRequest(
                new Request("/features/foo/bar", "GET", null, q("filter","name=foo","fields","value"), null),
                NanoHTTPD.HTTP_OK,
                NanoHTTPD.MIME_JSON
        );
        JSONObject parse = (JSONObject) new JSONParser().parse(new InputStreamReader(res.stream()));
        JSONArray features = (JSONArray) parse.get("features");
        JSONObject feature = (JSONObject) features.get(0);
        assertNull(feature.get("geometry"));
        assertEquals("22", feature.get("id"));
        JSONObject props = (JSONObject) feature.get("properties");
        assertEquals(1, props.keySet().size());
        assertEquals("bar", props.get("value"));
        mock.verify();
    }

    @Test
    public void testGetWorkspaceDatasetFeatureJSON() throws Exception {
        mock = MockServer.create()
                .withVectorLayer()
                    .withFeatureHavingId("baz")
                .replay();

        Response res = makeRequest(
                new Request("/features/foo/bar/baz", "GET", null, q(), null),
                NanoHTTPD.HTTP_OK,
                NanoHTTPD.MIME_JSON
        );

        Cursor<Feature> features = (Cursor<Feature>) new GeoJSONReader().read(res.stream());
        assertTrue(features.hasNext());
        Feature single = features.next();
        assertEquals(single.get("id"), "baz");
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
                .withPngRenderer()
                .replay();

        makeRequest(
                new Request("/features/foo/bar.png", "GET", null, q(), null),
                NanoHTTPD.HTTP_OK,
                NanoHTTPD.MIME_PNG
        );

        mock.verify();
    }

    @Test
    public void testPutEditFeature() throws Exception {
        Feature feature = new BasicFeature("baz");
        feature.put("name", "fourtytwo");
        mock = MockServer.create()
                .withVectorLayer()
                    .withFeatureHavingIdForEdit(feature, true)
                .replay();

        String json = dequote("{'type':'Feature'," +
            "'properties':{'name':'zero'}}");
        makeRequest(
                new Request("/features/foo/bar/baz", "PUT", h("Content-type", "application/json"), null, body(json)),
                NanoHTTPD.HTTP_OK,
                NanoHTTPD.MIME_PLAINTEXT
        );
        assertEquals("baz", feature.getId());
        assertEquals("zero", feature.get("name"));

        mock.verify();

        // make a bad request
        mock = MockServer.create()
                .withVectorLayer()
                    .withFeatureHavingIdForEdit(feature, false)
                .replay();
        makeBadRequest(
                new Request("/features/foo/bar/shaz", "PUT", h("Content-type", "application/json"), null, body(json)),
                NanoHTTPD.HTTP_NOTFOUND,
                "requested feature does not exist : /features/foo/bar/shaz"
        );
    }

    @Test
    public void testDeleteFeature() throws Exception {
        mock = MockServer.create()
                .withMemoryVectorLayer()
                .replay();

        makeRequest(
                new Request("/features/foo/bar/42", "DELETE", h("Content-type", "application/json"), null, null),
                NanoHTTPD.HTTP_OK,
                NanoHTTPD.MIME_PLAINTEXT
        );

        mock.verify();
        assertTrue(mock.memoryLayer.count(new VectorQuery()) == 0);

        mock = MockServer.create()
                .withMemoryVectorLayer()
                .replay();
        makeBadRequest(
                new Request("/features/foo/bar/66", "DELETE", h("Content-type", "application/json"), null, null),
                NanoHTTPD.HTTP_NOTFOUND,
                "requested feature does not exist : /features/foo/bar/66"
        );
        mock.verify();
    }

    @Test
    public void testPostAddFeatures() throws Exception {
        Schema schema = new SchemaBuilder("receiver").
                field("geometry", Point.class).
                field("name", String.class).
                schema();
        Feature receiver = new BasicFeature("receiver", schema);
        mock = MockServer.create()
                    .withWritableVectorLayer(receiver)
                .replay();

        String json = dequote("{'type':'Feature','geometry':{'type':'Point','coordinates':[1.2,3.4]}," +
            "'properties':{'name':'zero'}}");
        makeRequest(
                new Request("/features/foo/bar", "POST", h("Content-type", "application/json"), null, body(json)),
                NanoHTTPD.HTTP_CREATED,
                NanoHTTPD.MIME_PLAINTEXT
        );

        assertEquals("receiver{geometry=POINT (1.2 3.4), name=zero}", receiver.toString());

        mock.verify();
    }

    @Test
    public void testPutCreateLayer() throws Exception {
        mock = MockServer.create()
                    .withWorkspace()
                    .expectSchemaCreated()
                .replay();

        String json = dequote("{ 'type': 'schema', 'properties': { 'geometry': { 'type': 'Point' }, " +
            "'name': { 'type': 'string' } } }");

        makeRequest(
                new Request("/features/foo/widgets", "PUT", h("Content-type", "application/json"), null, body(json)),
                NanoHTTPD.HTTP_CREATED,
                NanoHTTPD.MIME_PLAINTEXT
        );

        mock.verify();

        mock = MockServer.create()
                    .withVectorLayer()
                .replay();
        makeBadRequest(
                new Request("/features/foo/bar", "PUT", h("Content-type", "application/json"), null, body(json)),
                NanoHTTPD.HTTP_BADREQUEST,
                "dataset 'bar' already exists in workspace foo"
        );
    }

    @Test
    public void testPostCreateLayer() throws Exception {
        mock = MockServer.create()
                    .withWorkspace()
                    .expectSchemaCreated()
                .replay();

        String json = dequote("{ 'type': 'schema','name': 'bar', 'properties': { 'geometry': { 'type': 'Point' }, " +
            "'name': { 'type': 'string' } } }");

        makeRequest(
                new Request("/features/foo", "POST", h("Content-type", "application/json"), null, body(json)),
                NanoHTTPD.HTTP_CREATED,
                NanoHTTPD.MIME_PLAINTEXT
        );

        mock.verify();

        mock = MockServer.create()
                    .withVectorLayer()
                .replay();
        makeBadRequest(
                new Request("/features/foo", "POST", h("Content-type", "application/json"), null, body(json)),
                NanoHTTPD.HTTP_BADREQUEST,
                "dataset 'bar' already exists in workspace foo"
        );
    }

    @Test
    public void testBadRequests() throws Exception {
        mock = MockServer.create()
                    .withVectorLayer()
                .replay();

        makeBadRequest(
                new Request("/features/baz", "GET", null, q(), null),
                NanoHTTPD.HTTP_NOTFOUND,
                "No such workspace: baz"
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
