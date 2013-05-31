package org.jeo.csv;

import java.util.ArrayList;
import java.util.List;

import org.jeo.feature.Field;
import org.jeo.geom.GeomBuilder;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Handles CSV file with lat/lon columns.
 * 
 * @author Justin Deoliveira, OpenGeo
 *
 */
public class XYHandler extends CSVHandler {
    CSVOptions opts;
    GeomBuilder gb;

    XYHandler(CSVOptions opts) {
        this.opts = opts;
        this.gb = new GeomBuilder();
    }

    @Override
    public List<Field> header(String[] head) {
        List<Field> fields = new ArrayList<Field>();
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

        for (String col: head) {
            fields.add(new Field(col, String.class));
        }
        return fields;
    }

    @Override
    public Geometry geom(String[] row) {
        return gb.point(Double.parseDouble(row[opts.getX()]), Double.parseDouble(row[opts.getY()])).toPoint();
    }
}
