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
