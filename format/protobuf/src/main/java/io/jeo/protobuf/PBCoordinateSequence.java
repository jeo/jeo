/* Copyright 2015 The jeo project. All rights reserved.
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
package io.jeo.protobuf;

import io.jeo.protobuf.Geom.Array;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.impl.PackedCoordinateSequence;

public class PBCoordinateSequence extends PackedCoordinateSequence {

    Array array;
    Envelope bounds;

    public PBCoordinateSequence(Array array) {
        this.array = array;
        this.dimension = array.getDim();
    }

    @Override
    public int size() {
        return array.getOrdCount() / array.getDim();
    }

    @Override
    public Envelope expandEnvelope(Envelope env) {
        env.expandToInclude(bounds());
        return env;
    }

    @Override
    public double getOrdinate(int index, int ordinate) {
        return array.getOrd(index * dimension + ordinate);
    }

    @Override
    protected Coordinate getCoordinateInternal(int index) {
        int i = index * dimension;
        return new Coordinate(array.getOrd(i), array.getOrd(i+1));
    }

    @Override
    public void setOrdinate(int index, int ordinate, double value) {
        throw new UnsupportedOperationException("read only coordinate sequence");
    }

    @Override
    public Object clone() {
        throw new UnsupportedOperationException();
    }

    Envelope bounds() {
        if (bounds == null) {
            double x1 = java.lang.Double.MAX_VALUE;
            double x2 = -java.lang.Double.MAX_VALUE;
            double y1 = java.lang.Double.MAX_VALUE;
            double y2 = -java.lang.Double.MAX_VALUE;

            for (int i = 0; i < array.getOrdCount(); i += dimension) {
                double x = array.getOrd(i);
                double y = array.getOrd(i+1);

                x1 = Math.min(x, x1);
                y1 = Math.min(y, y1);
                x2 = Math.max(x, x2);
                y2 = Math.max(y, y2);
            }

            bounds = new Envelope(x1, x2, y1, y2);
        }

        return bounds;
    }
}
