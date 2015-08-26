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
package io.jeo.data.dir;

import com.google.common.base.Charsets;
import com.google.common.collect.Iterables;
import io.jeo.TestData;
import io.jeo.Tests;
import io.jeo.data.Workspace;
import io.jeo.geojson.GeoJSONWriter;
import io.jeo.vector.VectorDataset;
import io.jeo.vector.VectorQuery;
import org.junit.Test;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DirWorkspaceTest {

    @Test
    public void test() throws IOException {
        Path dir = Tests.newTmpDir();
        writeTo(TestData.states(), dir);
        writeTo(TestData.point(), dir);
        writeTo(TestData.polygon(), dir);

        Workspace work = Directory.open(dir);
        assertEquals(3, Iterables.size(work.list()));
        assertNotNull(work.get("states"));
        assertNotNull(work.get("point"));
        assertNotNull(work.get("polygon"));
    }

    void writeTo(VectorDataset data, Path dir) throws IOException {
        try (Writer w = Files.newBufferedWriter(dir.resolve(data.name()+".json"), Charsets.UTF_8)) {
            new GeoJSONWriter(w)
                .featureCollection(data.read(VectorQuery.all()))
                .flush();
        }
    }
}
