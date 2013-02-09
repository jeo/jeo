package org.jeo.geopkg;

import java.io.IOException;

import org.jeo.data.Cursor;
import org.jeo.data.Vector;
import org.jeo.feature.Feature;
import org.jeo.feature.Schema;

import com.vividsolutions.jts.geom.Envelope;

public class GeoPkgVector implements Vector {

    FeatureEntry entry;
    GeoPackage geopkg;

    GeoPkgVector(FeatureEntry entry, GeoPackage geopkg) {
        this.entry = entry;
        this.geopkg = geopkg;
    }

    @Override
    public Schema getSchema() throws IOException {
        return geopkg.schema(entry);
    }

    @Override
    public Cursor<Feature> read(Envelope bbox) throws IOException {
        return geopkg.read(entry, bbox);
    }

    @Override
    public void add(Feature f) throws IOException {
        geopkg.add(entry, f);
    }

}
