/* Copyright 2013 The jeo project. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jeo.proj;

import org.osgeo.proj4j.CoordinateTransform;
import org.osgeo.proj4j.ProjCoordinate;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFilter;

/**
 * Applies a coordinate transform to the coordinates of a geometry object.
 * <p> 
 * This class isn't intended to be used directly but rather through {@link Proj#reproject()}}.
 * </p>
 * @see Proj#reproject(com.vividsolutions.jts.geom.Geometry, org.osgeo.proj4j.CoordinateReferenceSystem, org.osgeo.proj4j.CoordinateReferenceSystem)
 */
public class CoordinateTransformer implements CoordinateSequenceFilter, CoordinateFilter {

    CoordinateTransform tx;

    public CoordinateTransformer(CoordinateTransform tx) {
        this.tx = tx;
    }

    @Override
    public void filter(CoordinateSequence cs, int i) {
        ProjCoordinate p = new ProjCoordinate(cs.getX(i), cs.getY(i));
        tx.transform(p, p);

        cs.setOrdinate(i, 0, p.x);
        cs.setOrdinate(i, 1, p.y);
    }

    @Override
    public void filter(Coordinate coord) {
        ProjCoordinate p = new ProjCoordinate(coord.x, coord.y);
        tx.transform(p, p);

        coord.x = p.x;
        coord.y = p.y;
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
