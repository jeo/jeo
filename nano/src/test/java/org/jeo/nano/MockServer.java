package org.jeo.nano;

import com.google.common.collect.Iterators;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import org.easymock.IExpectationSetters;
import org.easymock.classextension.EasyMock;
import static org.easymock.classextension.EasyMock.createMock;
import org.jeo.data.Cursor;
import org.jeo.data.Cursors;
import org.jeo.data.DataRepository;
import org.jeo.data.Dataset;
import org.jeo.data.DatasetHandle;
import org.jeo.data.Driver;
import org.jeo.data.Query;
import org.jeo.data.Tile;
import org.jeo.data.TileDataset;
import org.jeo.data.TilePyramid;
import org.jeo.data.VectorDataset;
import org.jeo.data.Workspace;
import org.jeo.data.mem.MemVector;
import org.jeo.feature.BasicFeature;
import org.jeo.feature.Feature;
import org.jeo.feature.Features;
import org.jeo.feature.Field;
import org.jeo.feature.Schema;
import org.jeo.feature.SchemaBuilder;
import org.jeo.filter.Filter;
import org.jeo.filter.Id;
import org.jeo.filter.Literal;
import org.jeo.proj.Proj;

public class MockServer {

    private final List<Object> mocks = new ArrayList<Object>();
    final NanoServer server;
    MemVector memoryLayer;
    private VectorDataset vectorLayer;
    private TileDataset tileLayer;
    private Workspace workspace;
    private final DataRepository reg;

    private MockServer() {
        server = createMock(NanoServer.class);
        reg = createMock(DataRepository.class);
    }

    public static MockServer create() {
        return new MockServer();
    }

    private <T> T createMock(Class<T> clazz) {
        T mock = EasyMock.createMock(clazz);
        mocks.add(mock);
        return mock;
    }

    private <T> T createNiceMock(Class<T> clazz) {
        T mock = EasyMock.createNiceMock(clazz);
        mocks.add(mock);
        return mock;
    }

    MockServer withVectorLayer() throws Exception {
        withWorkspace();

        vectorLayer = createMock(VectorDataset.class);
        expect(workspace.get("bar")).andReturn(vectorLayer).once();
        expect(workspace.get((String) anyObject())).andReturn(null).anyTimes();
        vectorLayer.close();
        expectLastCall().once();

        return this;
    }

    MockServer withPointGeometry() throws Exception {
        expect(vectorLayer.crs()).andReturn(Proj.EPSG_4326);
        expect(vectorLayer.bounds()).andReturn(null);
        expect(vectorLayer.getName()).andReturn("emptylayer");
        Schema schema = createMock(Schema.class);
        Field field = new Field("geom", Point.class);
        expect(schema.geometry()).andReturn(field).atLeastOnce();
        expect(vectorLayer.schema()).andReturn(schema);
        return this;
    }

    MockServer withNoFeatures() throws Exception {
        expect(vectorLayer.cursor(new Query().bounds(new Envelope(-180, 180, -90, 90))))
                .andReturn(Cursors.empty(Feature.class)).once();

        return this;
    }

    MockServer withFeatureHavingId(String id) throws Exception {
        Feature f = new BasicFeature(id);
        f.put("id", id);

        Cursor<Feature> c = createMock(Cursor.class);
        expect(c.iterator()).andReturn(Iterators.forArray(f));
        expect(c.hasNext()).andReturn(true);
        
        expect(vectorLayer.cursor(new Query().filter(new Id(new Literal(id))))).andReturn(c);
        expect(vectorLayer.cursor((Query) anyObject())).andReturn(Cursors.empty(Feature.class)).anyTimes();
        c.close();
        expectLastCall().once();

        expect(vectorLayer.cursor(new Query().filter((Filter) anyObject())))
                .andReturn(Cursors.empty(Feature.class)).anyTimes();

        return this;
    }

    MockServer withFeatureHavingIdForEdit(Feature feature, boolean expectSuccess) throws Exception {
        Cursor<Feature> c = createMock(Cursor.class);
        expect(c.hasNext()).andReturn(Boolean.TRUE);
        expect(c.next()).andReturn(feature);
        expect(c.write()).andReturn(c);

        IExpectationSetters<Cursor<Feature>> cursor = 
                expect(vectorLayer.cursor(new Query().update().filter(new Id(new Literal(feature.getId()))))).andReturn(c);
        if (!expectSuccess) {
            cursor.anyTimes();
        }
        expect(vectorLayer.cursor((Query) anyObject())).andReturn(Cursors.empty(Feature.class)).anyTimes();
        c.close();
        expectLastCall().once();

        expect(vectorLayer.cursor(new Query().filter((Filter) anyObject())))
                .andReturn(Cursors.empty(Feature.class)).anyTimes();

        return this;
    }

    MockServer withSingleFeature() throws Exception {
        Feature f = createNiceMock(Feature.class);

        Cursor<Feature> c = createMock(Cursor.class);
        expect(c.next()).andReturn(f).once();
        expect(c.write()).andReturn(c).once();
        c.close();
        expectLastCall().once();

        expect(vectorLayer.cursor((Query) anyObject())).andReturn(c).once();

        return this;
    }

    MockServer withMoreDetails() throws Exception {
        Dataset active = vectorLayer == null ? tileLayer : vectorLayer;
        assert active != null : "expected a tile or vector layer";

        expect(active.getName()).andReturn("emptylayer").anyTimes();
        expect(active.bounds()).andReturn(new Envelope(-180, 180, -90, 90)).anyTimes();
        expect(active.crs()).andReturn(Proj.EPSG_4326).anyTimes();
        Driver driver = createMock(Driver.class);
        expect(driver.getName()).andReturn("mockDriver").anyTimes();
        expect(active.getDriver()).andReturn(driver).anyTimes();
        if (vectorLayer != null) {
            expect(vectorLayer.count((Query) anyObject())).andReturn(42L).anyTimes();
            Schema schema = createMock(Schema.class);
            Iterator<Field> fields = Iterators.forArray(
                    new Field("name", String.class)
            );
            expect(schema.iterator()).andReturn(fields).anyTimes();
            expect(vectorLayer.schema()).andReturn(schema).anyTimes();
        }
        return this;
    }

    MockServer replay() throws Exception {
        EasyMock.replay(mocks.toArray());
        return this;
    }

    void verify() {
        EasyMock.verify(mocks.toArray());
    }

    MockServer withWorkspace() throws Exception {
        workspace = createMock(Workspace.class);
        workspace.close();
        expectLastCall().atLeastOnce();

        expect(reg.get("foo")).andReturn(workspace).once();
        expect(reg.get((String) anyObject())).andReturn(null).anyTimes();
        expect(server.getRegistry()).andReturn(reg).atLeastOnce();

        return this;
    }

    MockServer expectSchemaCreated() throws Exception {
        VectorDataset layer = createMock(VectorDataset.class);
        expect(workspace.get((String) anyObject())).andReturn(null).anyTimes();
        expect(workspace.create((Schema) anyObject())).andReturn(layer).once();
        return this;
    }

    MockServer withSingleDataWorkspace() throws Exception {
        withWorkspace();

        Driver driver = createMock(Driver.class);
        expect(driver.getName()).andReturn("mockDriver");
        
        DatasetHandle dataSet = createMock(DatasetHandle.class);
        expect(dataSet.getName()).andReturn("mockDataSet");

        expect(workspace.getDriver()).andReturn(driver);
        expect(workspace.list()).andReturn(Collections.singleton(dataSet));

        return this;
    }

    MockServer withTileLayer(boolean expectTileAccess) throws Exception {
        withWorkspace();

        tileLayer = createMock(TileDataset.class);
        if (expectTileAccess) {
            expect(tileLayer.read(1, 2, 3)).andReturn(new Tile(1,2,3,new byte[]{},"image/png")).once();
        }
        tileLayer.close();
        expectLastCall().once();

        TilePyramid tilePyramid = createMock(TilePyramid.class);
        expect(tilePyramid.bounds((Tile) anyObject())).andReturn(new Envelope(-42, 42, -42, 42)).anyTimes();
        expect(tileLayer.pyramid()).andReturn(tilePyramid).anyTimes();

        expect(workspace.get("bar")).andReturn(tileLayer).once();

        return this;
    }

    MockServer withWritableVectorLayer(Feature receiver) throws Exception {
        withVectorLayer();

        Cursor<Feature> c = createMock(Cursor.class);
        expect(c.next()).andReturn(receiver);
        expect(c.write()).andReturn(c);

        expect(vectorLayer.cursor(new Query().append())).andReturn(c);
        c.close();
        expectLastCall().once();

        return this;
    }

    MockServer withMemoryVectorLayer() throws Exception {
        withWorkspace();

        Schema schema = new SchemaBuilder("memory")
                .field("name", String.class)
                .schema();
        memoryLayer = new MemVector(schema);
        memoryLayer.add(Features.create("42", schema, "foo"));
        expect(workspace.get("bar")).andReturn(memoryLayer).once();
        expect(workspace.get((String) anyObject())).andReturn(null).anyTimes();

        return this;
    }

}
