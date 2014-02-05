package org.jeo.geotools.render;

import static org.jeo.map.CartoCSS.*;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Panel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import org.jeo.TestData;
import org.jeo.data.Dataset;
import org.jeo.filter.Property;
import org.jeo.map.Map;
import org.jeo.map.RGB;
import org.jeo.map.Style;
import org.jeo.map.StyleBuilder;
import org.jeo.map.View;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.google.common.collect.Maps;

public class GTRendererTest {

    @Rule
    public TestName testName = new TestName();

    BufferedImage img;

    @Before
    public void setUp() {
        img = new BufferedImage(500, 500, BufferedImage.TYPE_4BYTE_ABGR);
    }

    @Test
    public void testPoints() throws Exception {
        Dataset points = TestData.point();

        Style style = new StyleBuilder().rule().select("*")
            .set(MARKER_WIDTH, 10).set(MARKER_FILL, "green")
            .set(MARKER_LINE_WIDTH, 2).set(MARKER_LINE_COLOR, "blue")
            .set(TEXT_NAME, new Property("name"))
            .set(TEXT_HORIZONTAL_ALIGNMENT, "right")
            .set(TEXT_VERTICAL_ALIGNMENT, "bottom")
            .style();

        View view = Map.build()
            .size(img.getWidth(),img.getHeight())
            .layer(points).style(style).view();

        render(view);
    }

    @Test
    public void testLines() throws Exception {
        Dataset lines = TestData.line();

        Style style = new StyleBuilder().rule().select("*")
            .set(LINE_WIDTH, 2).set(LINE_COLOR, RGB.red)
            .set(LINE_DASHARRAY, "2 3 1")
            .set(TEXT_NAME, new Property("name")).style();
        View view = Map.build()
            .size(img.getWidth(),img.getHeight())
            .layer(lines).style(style).view();

        render(view);
    }

    @Test
    public void testPolygons() throws Exception {
        Dataset polys = TestData.polygon();

        Style style = new StyleBuilder().rule().select("*")
            .set(POLYGON_FILL, "green").set(POLYGON_OPACITY, 0.5)
            .set(LINE_WIDTH, 2).set(LINE_COLOR, RGB.red).set(LINE_DASHARRAY, "2 3 1")
            .set(TEXT_NAME, new Property("name"))
            .set(TEXT_HALO_RADIUS, 2)
            .set(TEXT_HALO_FILL, RGB.white.alpha(0.5f))
            .style();

        View view = Map.build()
            .size(img.getWidth(),img.getHeight())
            .layer(polys).style(style).view();

        render(view);
    }

    void render(View v) throws Exception {
        java.util.Map<Object,Object> opts = Maps.newHashMap();
        opts.put(GTRendererFactory.IMAGE, img);

        GTRenderer r = new GTRendererFactory().create(v, opts);
        r.init(v, null);
        r.render(null);
    }

    @After
    public void show() {
        if (Boolean.getBoolean("java.awt.headless")) {
            return;
        }

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
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
    }
}
