package org.jeo.geom;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

public class PolygonPath extends CoordinatePath {

    /** the polygon */
    Polygon poly;

    /** coordinate index */
    int i;

    /** ring index, -1 means outer */
    int r;

    PolygonPath(Polygon poly) {
        this.poly = poly;
        doReset();
    }

    @Override
    public Geometry getGeometry() {
        return poly;
    }

    @Override
    protected PathStep doNext(Coordinate c) {
        LineString ring = ring();
        if (i < ring.getNumPoints()) {
            copy(ring, i, c);

            int j = i++;
            return j == 0 ? PathStep.MOVE_TO : 
                j == ring.getNumPoints()-1 ? PathStep.CLOSE : PathStep.LINE_TO;
        }
        else {
            //new path?
            if (poly.getNumInteriorRing() > 0 && r < poly.getNumInteriorRing()-1) {
                i=0; 
                r++;

                copy(ring(), i++, c);
                return PathStep.MOVE_TO;
            }
            else {
                return PathStep.STOP;
            }
        }
    }

    LineString ring() {
        return r == -1 ? poly.getExteriorRing() : poly.getInteriorRingN(r);
    }

    void copy(LineString ring, int i, Coordinate c) {
        Coordinate d = ring.getCoordinateN(i);
        c.x = d.x;
        c.y = d.y;
    }

    protected void doReset() {
        i = 0; 
        r = -1;
    }
}
