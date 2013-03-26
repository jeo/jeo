package org.jeo.nano;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.jeo.data.Cursor;
import org.jeo.data.Cursors;
import org.jeo.data.Query;
import org.jeo.data.Registry;
import org.jeo.data.Vector;
import org.jeo.data.Workspace;
import org.jeo.feature.Feature;
import org.jeo.feature.Schema;
import org.jeo.geojson.GeoJSON;
import org.jeo.nano.NanoHTTPD.Response;
import org.junit.Test;

import com.vividsolutions.jts.geom.Envelope;

public class FeatureHandlerTest extends HandlerTestSupport {

    @Test
    public void testGet() throws Exception {
        Vector layer = createMock(Vector.class);
        expect(layer.cursor(new Query().bounds(new Envelope(-180,180,-90,90))))
            .andReturn(Cursors.empty(Feature.class)).once();
        replay(layer);

        Workspace ws = createMock(Workspace.class);
        expect(ws.get("bar")).andReturn(layer).once();
        replay(ws);

        Registry reg = createMock(Registry.class);
        expect(reg.get("foo")).andReturn(ws).once();
        replay(reg);

        Request req = 
            new Request("/features/foo/bar", "GET", null, q("bbox=-180,-90,180,90"), null);
        FeatureHandler h = new FeatureHandler(reg);
        assertTrue(h.canHandle(req));

        Response res = h.handle(req);
        assertEquals(NanoHTTPD.HTTP_OK, res.status);
        
        List<Feature> features = (List<Feature>) GeoJSON.read(res.data);
        assertTrue(features.isEmpty());

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
        replay(c);

        Vector layer = createMock(Vector.class);
        expect(layer.cursor((Query)anyObject())).andReturn(c).once();
        replay(layer);

        Workspace ws = createMock(Workspace.class);
        expect(ws.get("bar")).andReturn(layer).once();
        replay(ws);

        Registry reg = createMock(Registry.class);
        expect(reg.get("foo")).andReturn(ws).once();
        replay(reg);

        String json = "{'type':'Feature','geometry':{'type':'Point','coordinates':[0.0,0.0]}," +
            "'properties':{'name':'zero'}}";
        Request req = new Request("/features/foo/bar", "POST", h("Content-type: application/json"), 
            null, body(json));
        FeatureHandler h = new FeatureHandler(reg);
        assertTrue(h.canHandle(req));

        Response res = h.handle(req);
        assertEquals(NanoHTTPD.HTTP_CREATED, res.status);

        verify(layer, ws, reg);
    }

    @Test
    public void testPostCreateLayer() throws Exception {
        Vector layer = createMock(Vector.class);
        replay(layer);

        Workspace ws = createMock(Workspace.class);
        expect(ws.create((Schema)anyObject())).andReturn(layer).once();
        replay(ws);

        Registry reg = createMock(Registry.class);
        expect(reg.get("foo")).andReturn(ws).once();
        replay(reg);

        String json = "{ 'type': 'schema','name': 'widgets', 'properties': { 'geometry': { 'type': 'Point' }, " +
            "'name': { 'type': 'string' } } }";

        Request req = new Request("/features/foo", "POST", h("Content-type: application/json"), 
            null, body(json));

        FeatureHandler h = new FeatureHandler(reg);
        assertTrue(h.canHandle(req));

        Response res = h.handle(req);
        assertEquals(NanoHTTPD.HTTP_CREATED, res.status);

        verify(layer, ws, reg);
    }
}
