package org.jeo.map.render;

import org.jeo.map.View;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;

/**
 * Filter that applies the affine transform of a {@link View}.
 *
 * @author Justin Deoliveira, Boundless
 */
public class ViewTransformFilter implements CoordinateFilter {

    View view;

    public ViewTransformFilter(View view) {
        this.view = view;
    }

    /**
     * Convenience method for chaining, calls {@link #filter(Coordinate)} and returns the 
     * original coordinate.
     */
    public Coordinate apply(Coordinate c) {
        filter(c);
        return c;
    }

    @Override
    public void filter(Coordinate coord) {
        coord.x = coord.x * view.scaleX() + view.translateX();
        coord.y = coord.y * -view.scaleY() + view.translateY();
    }

}
