package org.jeo.nano;

import static org.easymock.classextension.EasyMock.*;

import static org.junit.Assert.*;

import org.jeo.data.Cursor;
import org.jeo.data.Cursors;
import org.jeo.data.Query;
import org.jeo.data.Registry;
import org.jeo.data.VectorData;
import org.jeo.data.Workspace;
import org.jeo.feature.Feature;
import org.jeo.feature.Schema;
import org.jeo.geojson.GeoJSONReader;
import org.jeo.nano.NanoHTTPD.Response;
import org.junit.Test;

import com.vividsolutions.jts.geom.Envelope;

public class FeatureHandlerTest extends HandlerTestSupport {

    @Test
    public void testGet() throws Exception {
        VectorData layer = createMock(VectorData.class);
        expect(layer.cursor(new Query().bounds(new Envelope(-180,180,-90,90))))
            .andReturn(Cursors.empty(Feature.class)).once();
        replay(layer);

        Workspace ws = createMock(Workspace.class);
        expect(ws.get("bar")).andReturn(layer).once();
        replay(ws);

        Registry reg = createMock(Registry.class);
        expect(reg.get("foo")).andReturn(ws).once();
        replay(reg);

        NanoJeoServer server = createMock(NanoJeoServer.class);
        expect(server.getRegistry()).andReturn(reg).anyTimes();
        replay(server);

        Request req = 
            new Request("/features/foo/bar", "GET", null, q("bbox=-180,-90,180,90"), null);
        FeatureHandler h = new FeatureHandler();
        assertTrue(h.canHandle(req, server));

        Response res = h.handle(req, server);
        assertEquals(NanoHTTPD.HTTP_OK, res.status);
        
        Cursor<Feature> features = (Cursor<Feature>) new GeoJSONReader().read(res.data);
        assertFalse(features.hasNext());

        verify(layer, ws, reg);
    }

    @Test
    public void testPostAddFeatures() throws Exception {
        Feature f = createNiceMock(Feature.class);
        replay(f);

        Cursor<Feature> c = createMock(Cursor.class);
        expect(c.next()).andReturn(f).once();
        c.write();
        expectLastCall().once();
        c.close();
        expectLastCall().once();
        replay(c);

        VectorData layer = createMock(VectorData.class);
        expect(layer.cursor((Query)anyObject())).andReturn(c).once();
        replay(layer);

        Workspace ws = createMock(Workspace.class);
        expect(ws.get("bar")).andReturn(layer).once();
        replay(ws);

        Registry reg = createMock(Registry.class);
        expect(reg.get("foo")).andReturn(ws).once();
        replay(reg);

        NanoJeoServer server = createMock(NanoJeoServer.class);
        expect(server.getRegistry()).andReturn(reg).anyTimes();
        replay(server);

        String json = dequote("{'type':'Feature','geometry':{'type':'Point','coordinates':[0.0,0.0]}," +
            "'properties':{'name':'zero'}}");
        Request req = new Request("/features/foo/bar", "POST", h("Content-type: application/json"), 
            null, body(json));
        FeatureHandler h = new FeatureHandler();
        assertTrue(h.canHandle(req, server));

        Response res = h.handle(req, server);
        assertEquals(NanoHTTPD.HTTP_CREATED, res.status);

        verify(layer, ws, reg);
    }

    @Test
    public void testPostCreateLayer() throws Exception {
        VectorData layer = createMock(VectorData.class);
        replay(layer);

        Workspace ws = createMock(Workspace.class);
        expect(ws.create((Schema)anyObject())).andReturn(layer).once();
        replay(ws);

        Registry reg = createMock(Registry.class);
        expect(reg.get("foo")).andReturn(ws).once();
        replay(reg);

        NanoJeoServer server = createMock(NanoJeoServer.class);
        expect(server.getRegistry()).andReturn(reg).anyTimes();
        replay(server);

        String json = dequote("{ 'type': 'schema','name': 'widgets', 'properties': { 'geometry': { 'type': 'Point' }, " +
            "'name': { 'type': 'string' } } }");

        Request req = new Request("/features/foo", "POST", h("Content-type: application/json"), 
            null, body(json));

        FeatureHandler h = new FeatureHandler();
        assertTrue(h.canHandle(req, server));

        Response res = h.handle(req, server);
        assertEquals(NanoHTTPD.HTTP_CREATED, res.status);

        verify(layer, ws, reg);
    }

    String dequote(String json) {
        return json.replaceAll("'", "\"");
    }
}
