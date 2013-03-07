package org.jeo.geom;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class EmptyPath extends CoordinatePath {

    Geometry g;

    EmptyPath(Geometry g) {
        this.g = g;
    }

    @Override
    public Geometry getGeometry() {
        return g;
    }

    @Override
    protected PathStep doNext(Coordinate c) {
        return PathStep.STOP;
    }

    @Override
    protected void doReset() {
    }
}
