package org.jeo.java2d;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Panel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.awt.image.SinglePixelPackedSampleModel;

public class Java2D {

    public static BufferedImage packedImageARGB(int[] data, int width, int height) {
        DataBufferInt buf = new DataBufferInt(data, data.length);

        SinglePixelPackedSampleModel sampleModel = 
            new SinglePixelPackedSampleModel( DataBufferInt.TYPE_INT, width, height, 
            new int[]{0xff000000, 0x00ff0000, 0x0000ff00,0x000000ff});
        Raster raster = Raster.createRaster(sampleModel, buf, null);

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        img.setData(raster);
        return img;
    }

    public static Frame window(final BufferedImage img, String name) {
        Frame frame = new Frame(name != null ? name : "");
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
        return frame;
    }
}
