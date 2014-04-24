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

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.jeo.data.Dataset;
import org.jeo.data.Handle;
import org.jeo.feature.Schema;
import org.jeo.feature.SchemaBuilder;
import org.jeo.filter.Filter;
import org.jeo.map.Style;
import org.jeo.map.render.RendererFactory;
import org.jeo.proj.Proj;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.w3c.dom.Document;
import org.w3c.dom.Node;


public class WMSHandlerTest extends HandlerTestSupport {

    MockServer server;

    @Before
    public void init() {
        handler = new WMSHandler();
    }

    @Test
    public void testPattern() {
        assertTrue(handler.canHandle(new Request("/wms", "GET", q("service","wms")), null));
        assertTrue(handler.canHandle(new Request("/wms/", "GET", q("service","wms")), null));
    }

    private Request request(String... kv) {
        Properties parms = q(kv);
        parms.setProperty("service", "wms");
        parms.setProperty("version", "1.3.0");
        Properties header = new Properties();
        header.setProperty("host", "http://localhost:800");
        return new Request("/wms", "GET", header, parms, null);
    }

    private String wmsResponse(String... kv) throws Exception {
        NanoHTTPD.Response resp = handler.handle(request(kv), server == null ? null : server.server);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int r = 0;
        InputStream is = resp.stream();
        while ((r = is.read(buf)) > 0) {
            baos.write(buf, 0, r);
        }
        return new String(baos.toByteArray());
    }

    private void assertError(Request req, String... contents) throws Exception {
        try {
            handler.handle(req, server == null ? null : server.server);
            fail("expected HttpException");
        } catch (HttpException he) {
            Document dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(he.content.getBytes()));
            XPath xpath = XPathFactory.newInstance().newXPath();
            String message = xpath.evaluate("//ServiceException/text()", dom);
            Set<String> messages = new HashSet<String>(Arrays.asList(message.trim().split("\n")));
            for (int i = 0; i < contents.length; i++) {
                assertTrue("expected messages to contain " + contents[i] + " but was : " + message, messages.remove(contents[i]));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("expected HttpException");
        }
    }

    void makeTestData() throws Exception {
        server = MockServer.create();
        server.withPngRenderer();
        Schema s1 = new SchemaBuilder("s1").field("geom", Point.class, "EPSG:4326").schema();
        Schema s2 = new SchemaBuilder("s2").field("geom", LineString.class, "EPSG:3857").schema();
        Handle<Dataset> ds1 = server.createVectorDataset("ds1", "DataSet 1", new Envelope(-105, -100, 40, 45), s1);
        Handle<Dataset> ds2 = server.createVectorDataset("ds2", "DataSet 2", new Envelope(-42.42, -42, 42, 42.42), s2);
        server.createWorkspace("ws", ds1, ds2);
        server.buildRegistry().replay();
    }

    @Test
    public void testErrors() throws Exception {
        makeTestData();

        assertError(request(), "request parameter missing");
        assertError(request("request", "xyz"), "No operation : xyz");

        assertError(request("request", "getmap"),
                "Missing required parameter : layers",
                "Missing required parameter : srs",
                "Missing required parameter : width",
                "Missing required parameter : height"
        );

        assertError(request("request", "getmap", "bbox", "-153,19,-154,18", "srs", "epsg:4326"),
            "Invalid bbox, minx > maxx",
            "Invalid bbox, miny > maxy"
        );
    }

    @Test
    public void testCapabilities() throws Exception {
        makeTestData();
        String resp = wmsResponse("request","getcapabilities");
        Document dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(resp.getBytes()));
        XPath xpath = XPathFactory.newInstance().newXPath();
        Node n = (Node) xpath.evaluate("//Layer[Name/text()=\"ws:ds1\"]", dom, XPathConstants.NODE);
        assertNotNull(n);
        assertEquals("DataSet 1", xpath.evaluate("Title", n));
        n = (Node) xpath.evaluate("//Layer[Name/text()=\"ws:ds2\"]", dom, XPathConstants.NODE);
        assertNotNull(n);
        assertEquals("DataSet 2", xpath.evaluate("Title", n));
        assertEquals("image/png", xpath.evaluate("//GetMap/Format/text()", dom));
    }

    @Test
    public void testGetMap() throws Exception {
        makeTestData();
        final boolean[] called = new boolean[] {false};
        handler = new WMSHandler() {

            @Override
            NanoHTTPD.Response render(RendererFactory f, List<Dataset> dataSet, List<Style> styles,
                CoordinateReferenceSystem crs, Envelope bbox, int width, int height,
                String format, Filter filter) throws IOException {
                assertEquals("ds1", dataSet.get(0).getName());
                assertEquals(1, styles.size()); // generated style
                assertEquals(new Integer(4326), Proj.epsgCode(crs));
                assertEquals(512, width);
                assertEquals(256, height);
                assertEquals("image/png", format);
                assertNotNull(filter);
                called[0] = true;
                super.render(f, dataSet, styles, crs, bbox, width, height, format, filter);
                return null;
            }


        };
        handler.handle(request("request","getmap",
                               "layers","ws:ds1",
                               "styles","",
                               "crs","epsg:4326",
                               "bbox","-180,-90,180,90",
                               "width","512",
                               "height","256",
                               "cql_filter","VALUE > 5"), server.server);
        assertTrue(called[0]);
    }
}
