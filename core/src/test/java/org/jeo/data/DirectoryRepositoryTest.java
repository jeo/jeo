package org.jeo.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.easymock.classextension.EasyMock.*;

import java.io.File;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.easymock.IAnswer;
import org.jeo.Tests;
import org.jeo.filter.Filters;
import org.jeo.geojson.GeoJSON;
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
        assertEquals(2, Iterables.size(repo.query(Filters.all())));
        Iterables.find(repo.query(Filters.all()), new Predicate<Handle<Object>>() {
            @Override
            public boolean apply(Handle<Object> input) {
                return "foo".equals(input.getName()) 
                    && Dataset.class.isAssignableFrom(input.getType());
            }
        });
        Iterables.find(repo.query(Filters.all()), new Predicate<Handle<Object>>() {
            @Override
            public boolean apply(Handle<Object> input) {
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
}
