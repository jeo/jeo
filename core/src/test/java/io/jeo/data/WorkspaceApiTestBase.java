/* Copyright 2015 The jeo project. All rights reserved.
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
package io.jeo.data;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.vividsolutions.jts.geom.Point;
import io.jeo.proj.Proj;
import io.jeo.vector.Schema;
import io.jeo.vector.VectorDataset;
import io.jeo.vector.VectorDriver;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * Base test class for workspace implementations.
 */
public abstract class WorkspaceApiTestBase {

    Workspace work;

    @Before
    public final void setUp() throws Exception {
        init();
        work = createWorkspace();
    }

    protected void init() throws Exception {
    }

    protected abstract Workspace createWorkspace() throws Exception;


    @Test
    public void testCreate() throws Exception {
        Assume.assumeTrue(work.driver().capabilities().contains(VectorDriver.CREATE));

        Predicate<Handle<Dataset>> findPoints = new Predicate<Handle<Dataset>>() {
            @Override
            public boolean apply(Handle<Dataset> input) {
                return input.name().equals("points");
            }
        };

        if (work.driver().capabilities().contains(VectorDriver.DESTROY)) {
            if (Iterables.tryFind(work.list(), findPoints).isPresent()) {
                work.destroy("points");
            }
        }

        Schema schema = Schema.build("points")
            .field("geom", Point.class, Proj.EPSG_4326)
            .field("name", String.class)
            .schema();

        VectorDataset data = work.create(schema);
        assertNotNull(data);

        Iterables.find(work.list(), findPoints);

    }
}
