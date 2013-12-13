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

import java.util.Iterator;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * A sequence or path of coordinates. 
 * <p>
 * As the path is traversed a {@link #getStep()} is maintained that describes how the current 
 * coordinate relates to the previous coordinate.  
 * </p>
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public abstract class CoordinatePath implements Iterator<Coordinate> {

    /**
     * Enumeration describing path step/state.
     */
    public static enum PathStep {
        MOVE_TO, LINE_TO, CLOSE, STOP;
    }

    /**
     * Creates a coordinate iterator for the specified geometry.
     * <p>
     * This method calls through to {@link #create(Geometry, boolean, double, double)} with no 
     * generalization.
     * </p>
     */
    public static CoordinatePath create(Geometry g) {
        if (g == null || g.isEmpty()) {
            return new EmptyPath(g);
        }

        switch(Geom.Type.from(g)) {
        case POINT:
            return new PointPath((Point) g);
        case LINESTRING:
            return new LineStringPath((LineString)g);
        case POLYGON:
            return new PolygonPath((Polygon)g);
        case MULTIPOINT:
        case MULTILINESTRING:
        case MULTIPOLYGON:
        case GEOMETRYCOLLECTION:
            return new GeometryCollectionPath((GeometryCollection) g);
        default:
            throw new IllegalArgumentException("Unsupported type: " + g);
        }
    }

    protected PathStep step = null;

    protected Coordinate prev, curr;

    /**
     * decimation/generalization
     */
    protected boolean generalize = false;
    protected double dx = Double.NaN, dy = Double.NaN;

    /**
     * transform
     */
    protected CoordinateFilter tx;

    protected CoordinatePath() {
        prev = new Coordinate(Double.NaN, Double.NaN);
        curr = new Coordinate(Double.NaN, Double.NaN);
    }

    /**
     * Specifies that the coordinate path should be generalized skipping coordinates that 
     * are close to one another.
     * <p>
     * The <tt>dx</tt> and <tt>dy</tt> arguments are used as a tolerance to collapse coordinates. 
     * Consecutive coordinates with both horizontal and vertical distance less than <tt>dx</tt> and
     * <tt>dy</tt> respectively are collapsed.
     * </p> 
     * @param dx Horizontal generalization distance, only taken into account when 
     *   <tt>generalization</tt> is <tt>true</tt>
     * @param dy Vertical generalization distance, only taken into account when 
     *   <tt>generalization</tt> is <tt>true</tt>
     */
    public CoordinatePath generalize(double dx, double dy) {
        this.generalize = true;
        this.dx = dx;
        this.dy = dy;
        return this;
    }

    /**
     * Applies a transformation to the coordinate path.
     * 
     * @param tx Transformation/filter to apply to coordinates before returning.
     */
    public CoordinatePath transform(CoordinateFilter tx) {
        this.tx = tx;
        return this;
    }

    public abstract Geometry getGeometry();

    public Coordinate getCoordinate() {
        return curr;
    }

    public PathStep getStep() {
        return step;
    }

    @Override
    public boolean hasNext() {
        if (generalize && step == PathStep.LINE_TO || step == PathStep.MOVE_TO) {
            while(step == PathStep.LINE_TO || step == PathStep.MOVE_TO) {
                step = doNext(curr);
                if (!Double.isNaN(prev.x)) {
                    if (Math.abs(curr.x - prev.x) < dx && 
                        Math.abs(curr.y - prev.y) < dy) {
                        continue;
                    }
                }
                break;
            }
        }
        else {
            if (step != PathStep.STOP) {
                step = doNext(curr);
            }
        }

        return step != PathStep.STOP; 
    }

    @Override
    public Coordinate next() {
        if (step == PathStep.STOP) {
            return null;
        }

        prev.x = curr.x;
        prev.y = curr.y;

        // apply the filter
        if (tx != null) {
            tx.filter(curr);
        }

        return curr;
    }

    public void reset() {
        step = PathStep.MOVE_TO;
        doReset();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    protected abstract PathStep doNext(Coordinate c);
    
    protected abstract void doReset();
}
