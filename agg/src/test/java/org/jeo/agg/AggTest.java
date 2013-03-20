package org.jeo.agg;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.Panel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.awt.image.SinglePixelPackedSampleModel;

import org.jeo.data.Vector;
import org.jeo.java2d.Java2D;
import org.jeo.map.Map;
import org.jeo.map.MapBuilder;
import org.jeo.map.RGB;
import org.jeo.map.StyleBuilder;
import org.jeo.map.Stylesheet;
import org.jeo.shp.ShpData;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.rules.TestName;

public class AggTest {

    int TIMEOUT = 2000;
    
    @org.junit.Rule
    public TestName testName = new TestName();

    @BeforeClass
    public static void checkagg() {
        Assume.assumeTrue(AggRenderer.available());
    }

    @Test
    public void testPoint() throws Exception {
        Stylesheet style = new StyleBuilder().select("*")
            .set("marker-fill", RGB.blue)
            .set("marker-type", "circle")
            .set("marker-width", 20)
            .set("marker-line-color", RGB.black)
            .style();
        render(ShpData.point(), style);
    }

    @Test
    public void testLine() throws Exception {
        Stylesheet style = new StyleBuilder().select("*")
            .set("line-color", RGB.black)
            .set("line-width", 2.5)
            .style();
        render(ShpData.line(), style);
    }

    @Test
    public void testLineCased() throws Exception {
        Stylesheet style = new StyleBuilder().select("*")
            .set("line-color", RGB.black)
            .set("line-width", 5)
            .rule().select("::outer")
                .set("line-color", RGB.white)
                .set("line-width", 3)
            .endRule()
            
            .style();
        render(ShpData.line(), style);
    }

    @Test
    public void testLineDashed() throws Exception {
        Stylesheet style = new StyleBuilder().select("*")
            .set("line-color", RGB.blue)
            .set("line-width", 3)
            .set("line-dasharray", "5 2")
            .style();
        render(ShpData.line(), style);
    }

    @Test
    public void testSimple() throws Exception {
        Stylesheet style = new StyleBuilder()
            .rule().select("Map").set("background-color", "#b8dee6").endRule()
            .rule().select("#states")
                .rule().select("::outline")
                    .set("line-color", "#85c5d3")
                    .set("line-width", 2)
                    .set("line-join", "round")
                .endRule()
                .set("polygon-fill", "#fff")

            .endRule()
            .style();

        render(ShpData.states(), style);
    }

    @Test
    public void testThematic() throws Exception {
        Stylesheet style = new StyleBuilder()
            .rule().select("#states").select("::glow-outer")
                .set("line-color", "#226688")
                .set("line-opacity", 0.1)
                .set("line-join", "round")
                .set("line-width", 5)
            .endRule()
            .rule().select("#states").select("::glow-inner")
                .set("line-color", "#226688")
                .set("line-opacity", 0.8)
                .set("line-join", "round")
                .set("line-width", 2.4)
            .endRule()
            .rule().select("#states")
                .rule().filter("STATE_ABBR = 'IL'").set("polygon-fill", "#40e0d0").endRule()
                .rule().filter("STATE_ABBR = 'IL'").set("polygon-fill", "#f3c1d3").endRule()
                .rule().filter("STATE_ABBR = 'DC'").set("polygon-fill", "#a3cec5").endRule()
                .rule().filter("STATE_ABBR = 'DE'").set("polygon-fill", "#fae364").endRule()
                .rule().filter("STATE_ABBR = 'WV'").set("polygon-fill", "#aadb78").endRule()
                .rule().filter("STATE_ABBR = 'MD'").set("polygon-fill", "#ceb5cf").endRule()
                .rule().filter("STATE_ABBR = 'CO'").set("polygon-fill", "#f0f8ff").endRule()
                .rule().filter("STATE_ABBR = 'KY'").set("polygon-fill", "#f0f8ff").endRule()
                .rule().filter("STATE_ABBR = 'KS'").set("polygon-fill", "#f3c1d3").endRule()
                .rule().filter("STATE_ABBR = 'VA'").set("polygon-fill", "#fdaf6b").endRule()
                .rule().filter("STATE_ABBR = 'MO'").set("polygon-fill", "#a3cec5").endRule()
                .rule().filter("STATE_ABBR = 'AZ'").set("polygon-fill", "#d3e46f").endRule()
                .rule().filter("STATE_ABBR = 'OK'").set("polygon-fill", "#fae364").endRule()
                .rule().filter("STATE_ABBR = 'NC'").set("polygon-fill", "#ceb5cf").endRule()
                .rule().filter("STATE_ABBR = 'TN'").set("polygon-fill", "#f3c1d3").endRule()
                .rule().filter("STATE_ABBR = 'TX'").set("polygon-fill", "#d3e46f").endRule()
                .rule().filter("STATE_ABBR = 'NM'").set("polygon-fill", "#ceb5cf").endRule()
                .rule().filter("STATE_ABBR = 'AL'").set("polygon-fill", "#a3cec5").endRule()
                .rule().filter("STATE_ABBR = 'MS'").set("polygon-fill", "#fdc663").endRule()
                .rule().filter("STATE_ABBR = 'GA'").set("polygon-fill", "#f3c1d3").endRule()
                .rule().filter("STATE_ABBR = 'SC'").set("polygon-fill", "#a3cec5").endRule()
                .rule().filter("STATE_ABBR = 'AR'").set("polygon-fill", "#a3cec5").endRule()
                .rule().filter("STATE_ABBR = 'LA'").set("polygon-fill", "#fae364").endRule()
                .rule().filter("STATE_ABBR = 'FL'").set("polygon-fill", "#a3cec5").endRule()
                .rule().filter("STATE_ABBR = 'MI'").set("polygon-fill", "#a3cec5").endRule()
                .rule().filter("STATE_ABBR = 'MT'").set("polygon-fill", "#d3e46f").endRule()
                .rule().filter("STATE_ABBR = 'ME'").set("polygon-fill", "#a3cec5").endRule()
                .rule().filter("STATE_ABBR = 'ND'").set("polygon-fill", "#f3c1d3").endRule()
                .rule().filter("STATE_ABBR = 'SD'").set("polygon-fill", "#fdc663").endRule()
                .rule().filter("STATE_ABBR = 'WY'").set("polygon-fill", "#ceb5cf").endRule()
                .rule().filter("STATE_ABBR = 'WI'").set("polygon-fill", "#ceb5cf").endRule()
                .rule().filter("STATE_ABBR = 'ID'").set("polygon-fill", "#fae364").endRule()
                .rule().filter("STATE_ABBR = 'VT'").set("polygon-fill", "#f0f8ff").endRule()
                .rule().filter("STATE_ABBR = 'MN'").set("polygon-fill", "#ceb5cf").endRule()
                .rule().filter("STATE_ABBR = 'OR'").set("polygon-fill", "#f3c1d3").endRule()
                .rule().filter("STATE_ABBR = 'NH'").set("polygon-fill", "#a3cec5").endRule()
                .rule().filter("STATE_ABBR = 'IA'").set("polygon-fill", "#aadb78").endRule()
                .rule().filter("STATE_ABBR = 'MA'").set("polygon-fill", "#f3c1d3").endRule()
                .rule().filter("STATE_ABBR = 'NE'").set("polygon-fill", "#aadb78").endRule()
                .rule().filter("STATE_ABBR = 'NY'").set("polygon-fill", "#f0f8ff").endRule()
                .rule().filter("STATE_ABBR = 'PA'").set("polygon-fill", "#ceb5cf").endRule()
                .rule().filter("STATE_ABBR = 'CT'").set("polygon-fill", "#f0f8ff").endRule()
                .rule().filter("STATE_ABBR = 'RI'").set("polygon-fill", "#fdaf6b").endRule()
                .rule().filter("STATE_ABBR = 'NJ'").set("polygon-fill", "#f3c1d3").endRule()
                .rule().filter("STATE_ABBR = 'IN'").set("polygon-fill", "#fdaf6b").endRule()
                .rule().filter("STATE_ABBR = 'NV'").set("polygon-fill", "#f3c1d3").endRule()
                .rule().filter("STATE_ABBR = 'UT'").set("polygon-fill", "#f3c1d3").endRule()
                .rule().filter("STATE_ABBR = 'CA'").set("polygon-fill", "#fdaf6b").endRule()
                .rule().filter("STATE_ABBR = 'OH'").set("polygon-fill", "#f0f8ff").endRule()
                .rule().filter("STATE_ABBR = 'WA'").set("polygon-fill", "#aadb78").endRule()
            .endRule()
            .style();

        render(ShpData.states(), style);
    }

    void render(Vector l, Stylesheet s) {
        Map map = new MapBuilder().layer(l).style(s).map();

        AggRenderer r = new AggRenderer();
        r.init(map);
        r.render();

        show(img(r, map));
        r.dispose();
    }

    BufferedImage img(AggRenderer r, Map map) {
        return Java2D.packedImageARGB(r.data(), map.getWidth(), map.getHeight());
    }

    void show(final BufferedImage img) {
        if (!Boolean.getBoolean("java.awt.headless")) {
            try {
                Frame frame = Java2D.window(img, testName.getMethodName());
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
