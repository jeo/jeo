package org.jeo.raster;


import com.vividsolutions.jts.geom.Envelope;
import org.jeo.util.Dimension;

/**
 * Rectangle class.
 * <p>
 * Rectangle coordinates use an "image" coordinate system in which the top left corner
 * is considered (0,0) and the bottom right corner considered (width, height).
 * </p>
 */
public class Rect {

    public final int left, bottom, right, top;

    public Rect(int left, int top, int right, int bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    public Rect(int left, int top, Dimension size) {
        this.left = left;
        this.top = top;
        this.right = left + size.width();
        this.bottom = top + size.height();
    }

    public int width() {
        return right - left;
    }

    public int height() {
        return bottom - top;
    }

    public Rect map(Envelope bbox, Envelope world) {
        int l = (int)((bbox.getMinX() - world.getMinX())/world.getWidth() * width());
        int t = (int)((world.getMaxY() - bbox.getMaxY())/world.getHeight() * height());
        int w = (int)(bbox.getWidth() / world.getWidth() * width());
        int h = (int)(bbox.getHeight() / world.getHeight() * height());

        return new Rect(l, t, l+w, t+h);
    }
}
