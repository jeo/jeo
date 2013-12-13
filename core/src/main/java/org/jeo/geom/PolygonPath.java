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
