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
package org.jeo.cli;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import jline.console.ConsoleReader;

import org.jeo.data.Cursor;
import org.jeo.vector.VectorQuery;
import org.jeo.data.mem.MemVector;
import org.jeo.data.mem.MemWorkspace;
import org.jeo.data.mem.Memory;
import org.jeo.vector.Feature;
import org.jeo.vector.Schema;
import org.jeo.geom.Geom;
import org.junit.After;
import org.junit.Before;

import com.vividsolutions.jts.geom.Point;

/**
 * Base test for CLI command tests.
 * 
 * @author Justin Deoliveira, Boundless
 *
 */
public class CLITestSupport {

    protected PipedInputStream in;
    protected PipedOutputStream out;
    protected JeoCLI cli;

    @Before
    public void setUpCLI() throws IOException {
        in = new PipedInputStream();
        out = new PipedOutputStream(in);

        ConsoleReader in = new ConsoleReader(new ByteArrayInputStream(new byte[]{}), out);
        cli = new JeoCLI(in);
    }

    @Before
    public void setUpData() throws IOException {
        MemWorkspace mem = Memory.open("test");
        MemVector widgets = mem.create(Schema.build("cities")
            .field("geometry", Point.class).field("name", String.class).schema());

        Cursor<Feature> c = widgets.cursor(new VectorQuery().append());

        Feature f = c.next();
        f.put(Geom.point(-114, 51));
        f.put("name", "Calgary");
        f = c.write().next();

        f.put(Geom.point(-123, 48));
        f.put("name", "Vancouver");
        f = c.write().next();
        
        f.put(Geom.point(-79, 44));
        f.put("name", "Toronto");
        c.write().close();
    }

    public void tearDownCLI() throws IOException {
        out.close();
        in.close();
    }

    @After
    public void tearDownData() throws IOException {
        Memory.open("test").clear();
    }

    /**
     * Dumps current output stream to stdout.
     */
    protected void dump(OutputStream out) throws IOException {
        byte[] buf = new byte[1024];
        int r = -1;
        while(in.available() > 0 && (r = in.read(buf)) > 0) {
            out.write(buf, 0, r);
        }
    }
}
