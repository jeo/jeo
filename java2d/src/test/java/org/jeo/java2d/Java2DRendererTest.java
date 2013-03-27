package org.jeo.java2d;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Panel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.jeo.data.Cursor;
import org.jeo.data.Query;
import org.jeo.data.Vector;
import org.jeo.data.mem.Memory;
import org.jeo.feature.Feature;
import org.jeo.feature.Features;
import org.jeo.feature.Schema;
import org.jeo.geom.GeometryBuilder;
import org.jeo.map.Map;
import org.jeo.map.MapBuilder;
import org.jeo.map.RGB;
import org.jeo.map.StyleBuilder;
import org.jeo.map.Stylesheet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;


public class Java2DRendererTest {

    int TIMEOUT = 5000;

    @Rule
    public TestName testName = new TestName();

    Memory mem;
    Vector widgets;

    @Before
    public void setUp() throws IOException {
        mem = new Memory();

        GeometryBuilder gb = new GeometryBuilder();

        Schema schema = Features.schema(
            "widgets", "geo", Geometry.class, "name", String.class, "cost", Double.class);
        widgets = mem.create(schema);
        Cursor<Feature> c = widgets.cursor(new Query().append());
        
        Features.copy(
            Features.create(null, schema, gb.point(-45,-45).buffer(10), "bomb", 8.99), c.next());
        c.write();

        Features.copy(
            Features.create(null, schema, gb.point(20,-60).buffer(5), "anvil", 9.99), c.next());
        c.write();
       
        Features.copy(
            Features.create(null, schema, gb.point(0, 20).buffer(15), "bomb", 10.99), c.next());
        c.write();
        c.close();
    }

    @Test
    public void testSimple() throws Exception {

        Stylesheet style = new StyleBuilder().rule().select("*").
            set("line-width", 2).set("line-color",RGB.red)
            .style();
        Map map = new MapBuilder().size(500, 250).bounds(new Envelope(-180,180,-90,90))
            .layer(widgets).style(style).map();

        BufferedImage img = new BufferedImage(500, 250, BufferedImage.TYPE_4BYTE_ABGR);

        Java2DRenderer r = new Java2DRenderer((Graphics2D) img.getGraphics());
        r.init(map);
        r.render();

        show(img);
    }

    void show(final BufferedImage img) {
        if (!Boolean.getBoolean("java.awt.headless")) {
            try {
                Frame frame = new Frame(testName.getMethodName());
                frame.addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        e.getWindow().dispose();
                    }
                });
    
                Panel p = new Panel() {
                    {
                        setPreferredSize(new Dimension(img.getWidth(), img.getHeight()));
                    }
    
                    public void paint(Graphics g) {
                        g.drawImage(img, 0, 0, this);
                    }
                };
    
                frame.add(p);
                frame.pack();
                frame.setVisible(true);
    
                Thread.sleep(TIMEOUT);
                frame.dispose();
            } catch (HeadlessException exception) {
                // The test is running on a machine without X11 display. Ignore.
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }

}
