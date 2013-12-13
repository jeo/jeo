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
