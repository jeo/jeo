package org.jeo.csv;

import java.io.IOException;

import com.csvreader.CsvReader;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class WKTHandler extends CSVHandler {

    CSVOpts opts;

    WKTHandler(CSVOpts opts) {
        this.opts = opts;
    }

    @Override
    public void header(CsvReader r) throws IOException {
        if (opts.getWkt() == null) {
            Integer wkt = null;
            for (int i = 0; i < r.getHeaderCount(); i++) {
                String col = r.getHeader(i);
                if (col.equalsIgnoreCase(opts.getWktCol())) {
                    wkt = i;
                    break;
                }
            }

            if (wkt == null) {
                throw new IllegalStateException(String.format(
                    "Unable to determine wkt column from %s", opts.getWktCol()));
            }

            opts.wkt(wkt);
        }
    }

    @Override
    public Geometry geom(CsvReader r) throws IOException {
        String wkt = r.get(opts.getWkt());
        if ("".equals(wkt)) {
            return null;
        }

        try {
            return new WKTReader().read(wkt);
        } catch (ParseException e) {
            throw new IOException(e); 
        }
    }
}
