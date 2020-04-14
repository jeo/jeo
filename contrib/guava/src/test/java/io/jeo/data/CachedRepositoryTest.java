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
package io.jeo.data;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.io.IOException;

import io.jeo.data.Dataset;
import io.jeo.data.Workspace;
import org.junit.Test;

public class CachedRepositoryTest {

    @Test
    public void testWorkspace() throws IOException {
        Workspace ws = createMock(Workspace.class);

        DataRepository reg = createMock(DataRepository.class);
        expect(reg.get("foo", Workspace.class)).andReturn(ws).once();

        replay(ws, reg);

        CachedRepository cached = new CachedRepository(reg);
        assertNotNull(cached.get("foo", Workspace.class));
        assertNotNull(cached.get("foo", Workspace.class));
        assertNotNull(cached.get("foo", Workspace.class));

        verify(ws, reg);
    }

    @Test
    public void testLayer() throws IOException {
        Dataset l = createMock(Dataset.class);
        
        Workspace ws = createMock(Workspace.class);
        expect(ws.get("bar")).andReturn(l).once();

        DataRepository reg = createMock(DataRepository.class);
        expect(reg.get("foo", Workspace.class)).andReturn(ws).once();

        replay(l, ws, reg);

        CachedRepository cached = new CachedRepository(reg);
        assertNotNull(((Workspace)cached.get("foo", Workspace.class)).get("bar"));
        assertNotNull(((Workspace)cached.get("foo", Workspace.class)).get("bar"));
        assertNotNull(((Workspace)cached.get("foo", Workspace.class)).get("bar"));

        verify(l, ws, reg);
    }
}
