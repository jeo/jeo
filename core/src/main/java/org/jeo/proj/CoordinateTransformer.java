package org.jeo.proj;

import org.osgeo.proj4j.CoordinateTransform;
import org.osgeo.proj4j.ProjCoordinate;

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFilter;

public class CoordinateTransformer implements CoordinateSequenceFilter{

    CoordinateTransform tx;

    public CoordinateTransformer(CoordinateTransform tx) {
        this.tx = tx;
    }

    @Override
    public void filter(CoordinateSequence cs, int i) {
        ProjCoordinate p = new ProjCoordinate();
        p.x = cs.getX(i);
        p.y = cs.getY(i);

        tx.transform(p, p);

        cs.setOrdinate(i, 0, p.x);
        cs.setOrdinate(i, 1, p.y);
    }

    @Override
    public boolean isDone() {
       return false;
    }

    @Override
    public boolean isGeometryChanged() {
        return true;
    }

}
