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
import com.vividsolutions.jts.geom.GeometryCollection;

public class GeometryCollectionPath extends CoordinatePath {

    GeometryCollection gcol;

    CoordinatePath it;
    int i;

    GeometryCollectionPath(GeometryCollection gcol) {
        this.gcol = gcol;
        doReset();
    }

    @Override
    public Geometry geometry() {
        return gcol;
    }

    @Override
    protected PathStep doNext(Coordinate c) {
        while(!it.hasNext()) {
            if (++i == gcol.getNumGeometries()) {
                return PathStep.STOP;
            }

            it = CoordinatePath.create(gcol.getGeometryN(i));
        }

        return next(c);
    }

    PathStep next(Coordinate c) {
        Coordinate d = it.next();
        c.x = d.x;
        c.y = d.y;

        return it.step();
    }

    @Override
    protected void doReset() {
        i = 0;
        it = CoordinatePath.create(gcol.getGeometryN(i));
    }

}
