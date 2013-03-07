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

import org.jeo.geom.GeometryBuilder;
import org.jeo.map.Map;
import org.jeo.map.MapBuilder;
import org.jeo.map.RGB;
import org.jeo.map.Rule;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.rules.TestName;

import com.vividsolutions.jts.geom.Polygon;

public class AggTest {

    int TIMEOUT = 5000;
    
    @org.junit.Rule
    public TestName testName = new TestName();

    @BeforeClass
    public static void checkdb() {
        Throwable caught = null;
        try {
            System.loadLibrary("jeoagg");
        }
        catch(Throwable t) {
            caught = t;
        }
    
        Assume.assumeNoException(caught);
    }
    @Test
    public void test() {
        Map map = new MapBuilder().size(500, 256).map();
        AggRenderer r = new AggRenderer();
        r.init(map);

        Polygon poly = (Polygon) new GeometryBuilder().point(0,0).buffer(50);

        Rule rule = new Rule();
        rule.put("line-width", 2);
        rule.put("line-color", RGB.Green);
        rule.put("polygon-fill", RGB.Gray);

        r.drawPolygon(poly, null, rule);
        show(r, map);
    }

    void show(AggRenderer r, Map map) {
        int[] data = r.data();

        DataBufferInt buf = new DataBufferInt(data, data.length);
        
        SinglePixelPackedSampleModel sampleModel = 
            new SinglePixelPackedSampleModel( DataBufferInt.TYPE_INT, map.getWidth(), map.getHeight(), 
            new int[]{0x00ff0000, 0x0000ff00, 0x000000ff});
        Raster raster = Raster.createRaster(sampleModel, buf, null);

        BufferedImage img = new BufferedImage(map.getWidth(), map.getHeight(), BufferedImage.TYPE_INT_RGB);
        img.setData(raster);
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
