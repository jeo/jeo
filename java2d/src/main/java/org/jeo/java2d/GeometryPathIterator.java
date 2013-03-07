package org.jeo.java2d;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;

import org.jeo.geom.CoordinatePath;
import org.jeo.geom.Geom;

import com.vividsolutions.jts.geom.Coordinate;

public class GeometryPathIterator implements PathIterator {

    CoordinatePath path;
    AffineTransform at;

    double[] coords;

    public GeometryPathIterator(CoordinatePath path, AffineTransform at) {
        this.path = path;
        this.at = at;
        this.coords = new double[2];
    }

    @Override
    public int getWindingRule() {
        switch(Geom.Type.from(path.getGeometry())) {
        case POINT:
        case POLYGON:
            return WIND_EVEN_ODD;
        }

        return WIND_NON_ZERO;
    }

    @Override
    public boolean isDone() {
        return !path.hasNext();
    }

    @Override
    public void next() {
        path.next();
    }

    @Override
    public int currentSegment(float[] coords) {
        int r = currentSegment(this.coords);
        coords[0] = (float) this.coords[0];
        coords[1] = (float) this.coords[1];
        return r;
    }

    @Override
    public int currentSegment(double[] coords) {
        Coordinate c = path.getCoordinate();
        coords[0] = c.x;
        coords[1] = c.y;

        //apply the affine transform
        if (at != null) {
            at.transform(coords, 0, coords, 0, 1);
        }

        switch(path.getStep()) {
        case LINE_TO:
            return PathIterator.SEG_LINETO;
        case MOVE_TO:
            return PathIterator.SEG_MOVETO;
        case CLOSE:
            return PathIterator.SEG_CLOSE;
        default:
            throw new IllegalStateException(path.getStep().toString());
        }

    }

}
