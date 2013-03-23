package org.jeo.geogit;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.geogit.api.GeoGIT;
import org.geogit.api.porcelain.AddOp;
import org.geogit.api.porcelain.CommitOp;
import org.geogit.api.porcelain.ConfigOp;
import org.geogit.api.porcelain.ConfigOp.ConfigAction;
import org.geogit.di.GeogitModule;
import org.geogit.repository.Repository;
import org.geogit.storage.bdbje.JEStorageModule;
import org.geotools.util.NullProgressListener;
import org.jeo.Tests;
import org.jeo.data.Cursor;
import org.jeo.feature.Feature;
import org.jeo.geotools.GT;
import org.jeo.shp.Shapefile;
import org.jeo.shp.ShpData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.vividsolutions.jts.geom.Envelope;

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

        Shapefile shp = ShpData.states();
        repo.getWorkingTree().insert("states", GT.iterator(shp.read(null)), 
            new NullProgressListener(), null, null);
        shp.dispose();

        AddOp add = repo.command(AddOp.class);
        add.call();

        CommitOp commit = repo.command(CommitOp.class);
        commit.setMessage("initial commit");
        commit.call();

        ws = new GeoGit(gg);
    }

    @After
    public void tearDown() throws IOException {
        GeoGIT gg = ws.getGeoGIT();
        gg.close();
        FileUtils.deleteDirectory(gg.getPlatform().pwd());
    }

    @Test
    public void testLayers() throws IOException {
        assertTrue(Iterators.any(ws.layers(), new Predicate<String>() {
            @Override
            public boolean apply(String input) {
                return input.equals("states");
            }
        }));
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
        Set<String> stateNames = Sets.newHashSet(Iterables.transform(ShpData.states().read(null), 
            new Function<Feature, String>() {
                @Override
                public String apply(Feature input) {
                    return (String) input.get("STATE_NAME");
                }
            }));

        assertEquals(49, stateNames.size());
        for (Feature f : ws.get("states").read(null)) {
            stateNames.remove(f.get("STATE_NAME"));
        }

        assertTrue(stateNames.isEmpty());
    }
}
