package org.jeo.geom;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

public class LineStringPath extends CoordinatePath {

    /** original line string */
    LineString line;

    /** coordinate index */
    int i;

    LineStringPath(LineString line) {
        this.line = line;
        doReset();
    }

    @Override
    public Geometry getGeometry() {
        return line;
    }

    @Override
    protected PathStep doNext(Coordinate c) {
        if (i == line.getNumPoints()) {
            return PathStep.STOP;
        }

        Coordinate d = line.getCoordinateN(i);
        c.x = d.x;
        c.y = d.y;

        return i++ == 0 ? PathStep.MOVE_TO : PathStep.LINE_TO;
    }

    @Override
    protected void doReset() {
        i = 0;
    }
}
