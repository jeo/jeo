package org.jeo.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.easymock.classextension.EasyMock.*;

import java.io.File;
import java.io.FileWriter;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.easymock.IAnswer;
import org.jeo.Tests;
import org.jeo.filter.Filters;
import org.jeo.geojson.GeoJSON;
import org.jeo.json.JSONObject;
import org.jeo.json.JSONValue;
import org.jeo.map.Style;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

public class DirectoryRepositoryTest {

    DirectoryRepository repo;

    @Before
    public void setUp() throws Exception {
        File dir = Tests.newTmpDir("dir", "repo");
        FileUtils.touch(new File(dir, "foo.json"));
        FileUtils.touch(new File(dir, "bar.json"));

        repo = new DirectoryRepository(dir);
    }

    @After
    public void tearDown() throws Exception {
        repo.close();
    }

    @Test
    public void testList() throws Exception {
        assertEquals(2, Iterables.size(repo.list()));
        Iterables.find(repo.list(), new Predicate<Handle<?>>() {
            @Override
            public boolean apply(Handle<?> input) {
                return "foo".equals(input.getName()) 
                    && Dataset.class.isAssignableFrom(input.getType());
            }
        });
        Iterables.find(repo.list(), new Predicate<Handle<?>>() {
            @Override
            public boolean apply(Handle<?> input) {
                return "bar".equals(input.getName())
                    && Dataset.class.isAssignableFrom(input.getType());
            }
        });
    }

    @Test
    public void testGet() throws Exception {
        assertNotNull(repo.get("foo"));
        assertNotNull(repo.get("bar"));
        assertNull(repo.get("baz"));
    }

    @Test
    public void testUseRegistry() throws Exception {
        DriverRegistry drvreg = createMock(DriverRegistry.class);
        expect(drvreg.list()).andAnswer(new IAnswer<Iterator<Driver<?>>>() {
            @Override
            public Iterator<Driver<?>> answer() throws Throwable {
                return (Iterator) Iterators.singletonIterator(new GeoJSON());
            }
        }).times(3);
        replay(drvreg);

        repo = new DirectoryRepository(repo.getDirectory(), drvreg);
        repo.query(Filters.all());
        repo.get("foo");

        verify(drvreg);
    }

    @Test
    public void testMetaFile() throws Exception {
        FileUtils.touch(new File(repo.getDirectory(), "baz.foo"));

        createBazMetaFile();
        assertEquals(3, Iterables.size(repo.list()));
        assertNotNull(repo.get("baz"));
    }

    @Test
    public void testMetaFileSolo() throws Exception {
        createBazMetaFile();
        assertEquals(3, Iterables.size(repo.list()));
        assertNotNull(repo.get("baz"));
    }

    void createBazMetaFile() throws Exception {
        JSONObject meta = new JSONObject();
        meta.put("driver", "mem");

        JSONObject opts = new JSONObject();
        opts.put("name", "baz");
        meta.put("options", opts);

        FileWriter fw = new FileWriter(new File(repo.getDirectory(), "baz.jeo"));
        JSONValue.writeJSONString(meta, fw);
        fw.flush();
        fw.close();
    }

    @Test
    public void testStyles() throws Exception {
        final Driver<?> d = createNiceMock(Driver.class);
        expect(d.getName()).andReturn("css").anyTimes();
        expect(d.getType()).andReturn((Class)Style.class).anyTimes();
        replay(d);

        DirectoryRepository repo2 = new DirectoryRepository(repo.getDirectory(), 
            new DriverRegistry() {
                @Override
                public Iterator<Driver<?>> list() {
                    return (Iterator) Iterators.singletonIterator(d);
                }
            }, "css"
        );
        FileUtils.touch(new File(repo2.getDirectory(), "baz.css"));

        assertEquals(1, Iterables.size(repo2.list()));
        Iterables.find(repo2.list(), new Predicate<Handle<?>>() {
            @Override
            public boolean apply(Handle<?> input) {
                return "baz".equals(input.getName()) 
                    && Style.class.isAssignableFrom(input.getType());
            }
        });
        repo2.close();  
    }
}
