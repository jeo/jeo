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
package org.jeo.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.easymock.classextension.EasyMock.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.FileUtils;
import org.easymock.IAnswer;
import org.jeo.Tests;
import org.jeo.filter.Filters;
import org.jeo.geojson.GeoJSON;
import org.jeo.geojson.GeoJSONDataset;
import org.jeo.json.JSONObject;
import org.jeo.json.JSONValue;
import org.jeo.map.Style;
import org.jeo.util.Util;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.io.Files;

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
        
        Handle<?> found = Iterables.find(repo.query(Filters.all()), new Predicate<Handle<?>>() {
            @Override
            public boolean apply(Handle<?> input) {
                return "foo".equals(input.getName())
                        && Workspace.class.isAssignableFrom(input.getType());
            }
        });
        assertNotNull(found);

        found = Iterables.find(repo.query(Filters.all()), new Predicate<Handle<?>>() {
            @Override
            public boolean apply(Handle<?> input) {
                return "bar".equals(input.getName())
                    && Workspace.class.isAssignableFrom(input.getType());
            }
        });
        assertNotNull(found);
    }

    @Test
    public void testGet() throws Exception {
        assertNotNull(repo.get("foo", Workspace.class));
        assertNotNull(repo.get("bar", Workspace.class));
        assertNull(repo.get("baz", Workspace.class));
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
        repo.get("foo", Workspace.class);

        verify(drvreg);
    }

    @Test
    public void testMetaFile() throws Exception {
        FileUtils.touch(new File(repo.getDirectory(), "baz.foo"));

        createBazMetaFile();
        assertEquals(3, Iterables.size(repo.query(Filters.all())));
        assertNotNull(repo.get("baz", Workspace.class));
    }

    @Test
    public void testMetaFileSolo() throws Exception {
        createBazMetaFile();
        assertEquals(3, Iterables.size(repo.query(Filters.all())));
        assertNotNull(repo.get("baz", Workspace.class));
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

        assertEquals(1, Iterables.size(repo2.query(Filters.all())));
        Iterables.find(repo2.query(Filters.all()), new Predicate<Handle<?>>() {
            @Override
            public boolean apply(Handle<?> input) {
                return "baz".equals(input.getName()) 
                    && Style.class.isAssignableFrom(input.getType());
            }
        });
        repo2.close();
    }

    @Test
    public void testMetaFileWithOtherFile() throws Exception {
        File root = repo.getDirectory();
        FileUtils.deleteDirectory(root);

        root.mkdir();
        Files.touch(new File(root, "baz.abc"));
        Files.touch(new File(root, "baz.json"));

        JSONObject meta = new JSONObject();
        meta.put("driver", "geojson");

        JSONObject opts = new JSONObject();
        opts.put("file", "baz.json");
        meta.put("options", opts);

        FileWriter fw = new FileWriter(new File(repo.getDirectory(), "baz.jeo"));
        JSONValue.writeJSONString(meta, fw);
        fw.flush();
        fw.close();

        final AtomicBoolean hit = new AtomicBoolean(false);
        final Driver<?> d = new GeoJSON() {
            @Override
            public GeoJSONDataset open(File file, Map<?, Object> opts) throws IOException {
                hit.set(true);
                assertEquals("json", Util.extension(file.getName()));
                return super.open(file, opts);
            }
        };

        DirectoryRepository repo2 = new DirectoryRepository(root, new DriverRegistry() {
            @Override
            public Iterator<Driver<?>> list() {
                return (Iterator) Iterators.singletonIterator(d);
            }
        });

        repo2.get("baz", Object.class);
        assertTrue(hit.get());
    }
}
