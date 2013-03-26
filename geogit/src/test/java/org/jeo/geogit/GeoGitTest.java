package org.jeo.geogit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.geogit.api.GeoGIT;
import org.geogit.api.RevCommit;
import org.geogit.api.porcelain.AddOp;
import org.geogit.api.porcelain.BranchCreateOp;
import org.geogit.api.porcelain.CommitOp;
import org.geogit.api.porcelain.ConfigOp;
import org.geogit.api.porcelain.ConfigOp.ConfigAction;
import org.geogit.di.GeogitModule;
import org.geogit.repository.Repository;
import org.geogit.storage.bdbje.JEStorageModule;
import org.geotools.util.NullProgressListener;
import org.jeo.Tests;
import org.jeo.data.Cursor;
import org.jeo.data.Query;
import org.jeo.data.Transaction;
import org.jeo.feature.Feature;
import org.jeo.feature.Features;
import org.jeo.feature.Schema;
import org.jeo.geom.GeometryBuilder;
import org.jeo.geotools.GT;
import org.jeo.shp.Shapefile;
import org.jeo.shp.ShpData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Polygon;

public class GeoGitTest {

    GeoGit ws;

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

        addShp(ShpData.states(), repo);
        addShp(ShpData.point(), repo);
        addShp(ShpData.line(), repo);
        addShp(ShpData.poly(), repo);

        repo.command(BranchCreateOp.class).setName("scratch").call();
        ws = new GeoGit(gg);
    }

    void addShp(Shapefile shp, Repository repo) throws IOException {
        String name = shp.getName();
        repo.getWorkingTree().insert(name, GT.iterator(shp.cursor(new Query())), 
            new NullProgressListener(), null, null);
        shp.dispose();

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
        Set<String> layers = Sets.newHashSet(ws.layers());
        assertEquals(4, layers.size());
        assertTrue(layers.contains("states"));
        assertTrue(layers.contains("point"));
        assertTrue(layers.contains("line"));
        assertTrue(layers.contains("polygon"));
    }

    @Test
    public void testGet() throws Exception {
        GeoGitDataset states = ws.get("states");
        assertNotNull(states);
    }

    @Test
    public void testCount() throws Exception {
        assertEquals(49, ws.get("states").count(null));
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
        Set<String> names = Sets.newHashSet(Iterables.transform(ShpData.states().cursor(new Query()), 
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

        Transaction tx = points.transaction(null);
        Cursor<Feature> c = points.cursor(new Query().append().transaction(tx));

        Feature f = c.next();
        f.put("geometry", new GeometryBuilder().point(-114, 51));
        f.put("name", "Calgary");
        f.put("pop", 1214839l);
        c.write();

        f = c.next();
        f.put("geometry", new GeometryBuilder().point(-123, 49));
        f.put("name", "Vancouver");
        f.put("pop", 2313328l);
        c.write();

        tx.commit();

        points = ws.get("point");
        assertEquals(count+2, points.count(new Query()));

        assertEquals(1, points.count(new Query().filter("name = 'Calgary'")));
        assertEquals(1, points.count(new Query().filter("name = 'Vancouver'")));
    }

    @Test
    public void testCreate() throws Exception {
        Schema widgets = Features.schema("widgets", "shape", Polygon.class, "name", String.class, 
            "cost", Double.class);
        GeoGitDataset data = ws.create(widgets);
        assertEquals(0, data.count(new Query()));

        GeometryBuilder gb = new GeometryBuilder();

        Transaction tx = data.transaction(null);
        Cursor<Feature> c = data.cursor(new Query().append().transaction(tx));

        Feature f = c.next();
        f.put("shape", gb.point(0,0).buffer(10));
        f.put("name", "bomb");
        f.put("cost", 1.99);
        c.write();

        f = c.next();
        f.put("shape", gb.lineString(0,0,1,1).buffer(1));
        f.put("name", "dynamite");
        f.put("cost", 2.99);
        c.write();

        f = c.next();
        f.put("shape", gb.polygon(-5,5, 5,5, 2,-2, 3,-5, -3,-5, -2,-2, -5,5));
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
