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
package org.jeo.nano;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import static org.jeo.nano.NanoHTTPD.HTTP_REDIRECT;
import org.jeo.nano.NanoHTTPD.Response;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Ian Schneider <ischneider@boundlessgeo.com>
 */
public class AppsHandlerTest extends HandlerTestSupport {

    @Before
    public void init() throws IOException {
        handler = new DataHandler();
    }
    
    static File dir(Object... path) {
        File f = new File(path[0].toString());
        f.deleteOnExit();
        for (int i = 1; i < path.length; i++) {
            f = new File(f, path[i].toString());
            f.mkdirs();
            f.deleteOnExit();
        }
        return f;
    }
    
    static void touch(File parent, String path) throws IOException {
        File f = new File(parent, path);
        f.createNewFile();
        f.deleteOnExit();
    }

    @Test
    public void testCanHandle() {
        handler.canHandle(new Request("/apps", null), null);
        handler.canHandle(new Request("/apps/foobar", null), null);
    }

    @Test
    public void testServe() throws IOException {
        File root = dir("target", "data" + System.currentTimeMillis());
        File nestedDir = dir(root, "nested333", "data678");
        touch(root, "file123");
        touch(nestedDir, "file456");

        Response r = NanoHTTPD.serveFile("/apps", null, root, true, "/apps");
        assertEquals(HTTP_REDIRECT, r.status);
        assertEquals("/apps/", r.header.getProperty("Location"));

        r = NanoHTTPD.serveFile("/apps/", h(), root, true, "/apps");
        assertEquals("200 OK", r.status);
        String data = new Scanner(r.stream()).useDelimiter("\\Z").next();
        assertTrue(data.contains("nested333"));
        assertTrue(data.contains("file123"));

        r = NanoHTTPD.serveFile("/apps/file123", h(), root, true, "/apps");
        assertEquals("200 OK", r.status);
        assertEquals(-1, r.stream().read());

        r = NanoHTTPD.serveFile("/apps/nested333/data678", h(), root, true, "/apps");
        assertEquals(HTTP_REDIRECT, r.status);
        assertEquals("/apps/nested333/data678/", r.header.getProperty("Location"));

        r = NanoHTTPD.serveFile("/apps/nested333/data678/file456", h(), root, true, "/apps");
        assertEquals("200 OK", r.status);
        assertEquals(-1, r.stream().read());
    }
}
