package org.jeo.csv;

import java.util.List;

import org.jeo.geom.GeomBuilder;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Handles CSV file with lat/lon columns.
 * 
 * @author Justin Deoliveira, OpenGeo
 *
 */
public class XYHandler extends CSVHandler {
    CSVOpts opts;
    GeomBuilder gb;

    XYHandler(CSVOpts opts) {
        this.opts = opts;
        this.gb = new GeomBuilder();
    }

    @Override
    public void header(String[] head) {
        if (opts.getX() == null) {
            Integer x = null, y = null;
            for (int i = 0; i < head.length; i++) {
                if (head[i].equalsIgnoreCase(opts.getXcol())) {
                    x = i;
                }
                if (head[i].equalsIgnoreCase(opts.getYcol())) {
                    y = i;
                }
            }

            if (x == null || y == null) {
                throw new IllegalStateException(String.format(
                    "Unable to determine x, y columns from %s, %s", opts.getXcol(), opts.getYcol()));
            }

            opts.xy(x, y);
        }
    }

    @Override
    public Geometry geom(List<Object> row) {
        Number x = (Number) row.get(opts.getX());
        Number y = (Number) row.get(opts.getY());
        return gb.point(x.doubleValue(), y.doubleValue()).toPoint();
    }
}
