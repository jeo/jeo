package org.jeo.cli;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import jline.console.ConsoleReader;

import org.jeo.data.Cursor;
import org.jeo.data.Query;
import org.jeo.data.mem.MemVector;
import org.jeo.data.mem.MemWorkspace;
import org.jeo.data.mem.Memory;
import org.jeo.feature.Feature;
import org.jeo.feature.Schema;
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
        MemWorkspace mem = Memory.open();
        MemVector widgets = mem.create(Schema.build("cities")
            .field("geometry", Point.class).field("name", String.class).schema());

        Cursor<Feature> c = widgets.cursor(new Query().append());

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
        Memory.open().clear();
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
