package org.jeo.geom;

import java.util.Iterator;

import com.vividsolutions.jts.geom.Coordinate;
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
        return create(g, false, Double.NaN, Double.NaN);
    }

    /**
     * Creates coordinate iterator for the specified geometry.
     * <p>
     * The <tt>generalize</tt> parameter specifies whether or not the geometry should be 
     * generalized resulting in fewer coordinates being returned. When set to <tt>true</tt> the 
     * <tt>dx</tt> and <tt>dy</tt> arguments are used as a tolerance to collapse coordinates. 
     * Consecutive coordinates with both horizontal and vertical distance less than <tt>dx</tt> and
     * <tt>dy</tt> respectively are collapsed.  
     * </p> 
     * @param g The geometry.
     * @param generalize Generalization flag.
     * @param dx Horizontal generalization distance, only taken into account when 
     *   <tt>generalization</tt> is <tt>true</tt>
     * @param dy Vertical generalization distance, only taken into account when 
     *   <tt>generalization</tt> is <tt>true</tt>
     * @return
     */
    public static CoordinatePath create(Geometry g, boolean generalize, double dx, double dy) {
        switch(Geom.Type.fromObject(g)) {
        case POINT:
            return new PointPath((Point) g);
        case LINESTRING:
            return new LineStringPath((LineString)g, generalize, dx, dy);
        case POLYGON:
            return new PolygonPath((Polygon)g, generalize, dx, dy);
        case MULTIPOINT:
        case MULTILINESTRING:
        case MULTIPOLYGON:
        case GEOMETRYCOLLECTION:
            return new GeometryCollectionPath((GeometryCollection) g, generalize, dx, dy);
        default:
            throw new IllegalArgumentException("Unsupported type: " + g);
        }
    }

    protected PathStep step = null;

    protected Coordinate prev, curr;

    protected boolean generalize = false;
    protected double dx, dy;
    
    protected CoordinatePath() {
        this(false, Double.NaN, Double.NaN);
    }

    protected CoordinatePath(boolean generalize, double dx, double dy) {
        this.generalize = generalize;
        this.dx = dx;
        this.dy = dy;

        prev = new Coordinate(Double.NaN, Double.NaN);
        curr = new Coordinate(Double.NaN, Double.NaN);
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
