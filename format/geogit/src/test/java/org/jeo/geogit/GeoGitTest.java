package org.jeo.geogit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.geogit.api.GeoGIT;
import org.geogit.api.RevCommit;
import org.geogit.api.plumbing.diff.Patch;
import org.geogit.api.porcelain.AddOp;
import org.geogit.api.porcelain.BranchCreateOp;
import org.geogit.api.porcelain.CommitOp;
import org.geogit.api.porcelain.ConfigOp;
import org.geogit.api.porcelain.ConfigOp.ConfigAction;
import org.geogit.api.porcelain.LogOp;
import org.geogit.di.GeogitModule;
import org.geogit.repository.Repository;
import org.geogit.storage.bdbje.JEStorageModule;
import org.geotools.util.NullProgressListener;
import org.jeo.TestData;
import org.jeo.Tests;
import org.jeo.data.Cursor;
import org.jeo.data.Query;
import org.jeo.data.Transaction;
import org.jeo.data.VectorData;
import org.jeo.feature.Feature;
import org.jeo.feature.Schema;
import org.jeo.feature.SchemaBuilder;
import org.jeo.geom.GeomBuilder;
import org.jeo.geotools.GT;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Polygon;

public class GeoGitTest {

    GeoGitWorkspace ws;

    @Before
    public void setUp() throws IOException {
        Injector i = Guice.createInjector(
            Modules.override(new GeogitModule()).with(new JEStorageModule()));

        GeoGIT gg = new GeoGIT(i, Tests.newTmpDir("geogit", "tmp")); 

        Repository repo = gg.getOrCreateRepository();
        repo.command(ConfigOp.class).setAction(ConfigAction.CONFIG_SET)
            .setName("user.name").setValue("Wile E Coyote").call();
        repo.command(ConfigOp.class).setAction(ConfigAction.CONFIG_SET).setName("user.email")
            .setValue("wile@acme.com").call();

        addShp(TestData.states(), repo);
        addShp(TestData.point(), repo);
        addShp(TestData.line(), repo);
        addShp(TestData.polygon(), repo);

        repo.command(BranchCreateOp.class).setName("scratch").call();
        ws = new GeoGitWorkspace(gg);
    }

    void log(GeoGIT gg) {
        for (Iterator<RevCommit> it = gg.command(LogOp.class).call(); it.hasNext();) {
            System.out.println(it.next());
        }
    }

    void addShp(VectorData data, Repository repo) throws IOException {
        String name = data.getName();
        repo.getWorkingTree().insert(name, GT.iterator(data.cursor(new Query())), 
            new NullProgressListener(), null, null);
        data.close();

        AddOp add = repo.command(AddOp.class);
        add.call();

        CommitOp commit = repo.command(CommitOp.class);
        commit.setMessage("initial commit of " + name);
        commit.call();
    }

    @After
    public void tearDown() throws IOException {
        GeoGIT gg = ws.getGeoGIT();
        gg.close();
        FileUtils.deleteDirectory(gg.getPlatform().pwd());
    }

    @Test
    public void testBranches() throws IOException {
        Set<String> branches = Sets.newHashSet(ws.branches());
        assertEquals(2, branches.size());
        assertTrue(branches.contains("master"));
        assertTrue(branches.contains("scratch"));
    }

    @Test
    public void testLayers() throws IOException {
        Set<String> layers = Sets.newHashSet(ws.list());
        assertEquals(4, layers.size());
        assertTrue(layers.contains("states"));
        assertTrue(layers.contains("point"));
        //assertTrue(layers.contains("line"));
        //assertTrue(layers.contains("polygon"));
    }

    @Test
    public void testGet() throws Exception {
        GeoGitDataset states = ws.get("states");
        assertNotNull(states);
    }

    @Test
    public void testGetAtRevision() throws Exception {
        Iterator<RevCommit> it = ws.log("master").call();
        assertTrue(it.hasNext());
        it.next();
        assertTrue(it.hasNext());
        
        RevCommit second = it.next(); 
        RevCommit last = Iterators.getLast(it);

        assertNotNull(ws.get("point"));
        assertNotNull(ws.get("point@" + second.getId().toString()));
        assertNull(ws.get("point@" + last.getId().toString()));
    }

    @Test
    public void testCount() throws Exception {
        assertEquals(49, ws.get("states").count(new Query()));
    }

    @Test
    public void testCountAtRevision() throws Exception {
        GeoGitDataset data = ws.get("point");
        
        long prevCount = data.count(new Query());
        String prev = data.getRevision().getId().toString();

        doAddPoints();

        data = ws.get("point");
        assertEquals(prevCount + 2, data.count(new Query()));

        data = ws.get("point@" + prev);
        assertEquals(prevCount, data.count(new Query()));
    }

    @Test
    public void testPatch() throws Exception {
        doAddPoints();

        Patch p = ws.patch("point", ws.get("point").getRevision().getId().toString());
        assertEquals(2, p.getAddedFeatures().size());
    }

    @Test
    public void testBounds() throws Exception {
        Envelope bounds = ws.get("states").bounds();
        assertEquals(-124.73, bounds.getMinX(), 0.01);
        assertEquals(24.96, bounds.getMinY(), 0.01);
        assertEquals(-66.97, bounds.getMaxX(), 0.01);
        assertEquals(49.37, bounds.getMaxY(), 0.01);
    }

    @Test
    public void testReadAll() throws Exception {
        Set<String> names = Sets.newHashSet(Iterables.transform(TestData.states().cursor(new Query()), 
            new Function<Feature, String>() {
                @Override
                public String apply(Feature input) {
                    return (String) input.get("STATE_NAME");
                }
            }));

        assertEquals(49, names.size());
        for (Feature f : ws.get("states").cursor(new Query())) {
            names.remove(f.get("STATE_NAME"));
        }

        assertTrue(names.isEmpty());
    }

    @Test
    public void testUpdate() throws Exception {
        GeoGitDataset states = ws.get("states");
        Set<String> abbrs = Sets.newHashSet(Iterables.transform(states.cursor(new Query()),
            new Function<Feature, String>() {
                @Override
                public String apply(Feature input) {
                    String abbr = (String) input.get("STATE_ABBR");
                    assertNotNull(abbr);
                    assertNotEquals(abbr, abbr.toLowerCase());

                    return abbr.toLowerCase();
                }
            }));

        Transaction tx = states.transaction(null);
        Cursor<Feature> c = states.cursor(new Query().update().transaction(tx));
        for (Feature f : c) {
            f.put("STATE_ABBR", ((String)f.get("STATE_ABBR")).toLowerCase());
            c.write();
        }
        tx.commit();

        assertEquals(49, abbrs.size());
        for (Feature f : ws.get("states").cursor(new Query())) {
            abbrs.remove(f.get("STATE_ABBR"));
        }

        assertTrue(abbrs.isEmpty());
    }

    @Test
    public void testAppend() throws Exception {
        GeoGitDataset points = ws.get("point");
        
        long count = points.count(new Query());

        doAddPoints();

        points = ws.get("point");
        assertEquals(count+2, points.count(new Query()));

        assertEquals(1, points.count(new Query().filter("name = 'Calgary'")));
        assertEquals(1, points.count(new Query().filter("name = 'Vancouver'")));
    }

    void doAddPoints() throws Exception {
        GeoGitDataset points = ws.get("point");

        Transaction tx = points.transaction(null);
        Cursor<Feature> c = points.cursor(new Query().append().transaction(tx));

        Feature f = c.next();
        f.put("geometry", new GeomBuilder().point(-114, 51).toPoint());
        f.put("name", "Calgary");
        f.put("pop", 1214839l);
        c.write();

        f = c.next();
        f.put("geometry", new GeomBuilder().point(-123, 49).toPoint());
        f.put("name", "Vancouver");
        f.put("pop", 2313328l);
        c.write();

        tx.commit();
    }

    @Test
    public void testCreate() throws Exception {
        Schema widgets = new SchemaBuilder("widgets").field("shape", Polygon.class)
            .field("name", String.class).field("cost", Double.class).schema();

        GeoGitDataset data = ws.create(widgets);
        assertEquals(0, data.count(new Query()));

        GeomBuilder gb = new GeomBuilder();

        Transaction tx = data.transaction(null);
        Cursor<Feature> c = data.cursor(new Query().append().transaction(tx));

        Feature f = c.next();
        f.put("shape", gb.point(0,0).toPoint().buffer(10));
        f.put("name", "bomb");
        f.put("cost", 1.99);
        c.write();

        f = c.next();
        f.put("shape", gb.points(0,0,1,1).toLineString().buffer(1));
        f.put("name", "dynamite");
        f.put("cost", 2.99);
        c.write();

        f = c.next();
        f.put("shape", gb.points(-5,5, 5,5, 2,-2, 3,-5, -3,-5, -2,-2, -5,5).ring().toPolygon());
        f.put("name", "anvil");
        f.put("cost", 3.99);

        c.write();
        tx.commit();

        data = ws.get("widgets");
        assertEquals(3, data.count(new Query()));

        c = data.cursor(new Query().filter("name = 'bomb'"));
        assertTrue(c.hasNext());
        assertEquals(1.99, c.next().get("cost"));
        
    }
}
