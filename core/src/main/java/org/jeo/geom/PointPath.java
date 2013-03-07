package org.jeo.geom;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public class PointPath extends CoordinatePath {

    Point p;
    boolean b;

    PointPath(Point p) {
        this.p = p;
        doReset();
    }

    @Override
    public Geometry getGeometry() {
        return p;
    }

    @Override
    protected PathStep doNext(Coordinate c) {
        if (b) {
            c.x = p.getX();
            c.y = p.getY();

            b = false;
            return PathStep.MOVE_TO;
        }
        return PathStep.STOP;
    }

    @Override
    protected void doReset() {
        b = true;
    }
}
