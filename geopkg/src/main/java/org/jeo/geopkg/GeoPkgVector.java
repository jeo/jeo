package org.jeo.geopkg;

import java.io.IOException;

import org.jeo.data.Cursor;
import org.jeo.data.Query;
import org.jeo.data.Vector;
import org.jeo.data.Cursor.Mode;
import org.jeo.feature.Feature;
import org.jeo.feature.Schema;
import org.jeo.proj.Proj;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

public class GeoPkgVector implements Vector {

    FeatureEntry entry;
    GeoPackage geopkg;

    GeoPkgVector(FeatureEntry entry, GeoPackage geopkg) {
        this.entry = entry;
        this.geopkg = geopkg;
    }

    @Override
    public String getName() {
        return entry.getTableName();
    }

    @Override
    public String getTitle() {
        return entry.getIdentifier();
    }

    @Override
    public String getDescription() {
        return entry.getDescription();
    }

    @Override
    public Schema getSchema() throws IOException {
        return geopkg.schema(entry);
    }

    @Override
    public CoordinateReferenceSystem getCRS() {
        int srid = entry.getSrid();
        return srid != -1 ? Proj.crs(srid) : null;
    }

    @Override
    public Envelope bounds() throws IOException {
        return entry.getBounds();
    }

    @Override
    public long count(Query q) throws IOException {
        return geopkg.count(entry, q);
    }

    @Override
    public boolean supports(Mode mode) {
        return mode == Cursor.READ;
    }

    @Override
    public Cursor<Feature> cursor(Query q, Cursor.Mode mode) throws IOException {
        return geopkg.cursor(entry, q, mode);
    }

    @Override
    public void add(Feature f) throws IOException {
        geopkg.add(entry, f);
    }

}
