package org.jeo.data;

import java.io.IOException;

import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Handle for a {@link Dataset} object.
 * 
 * @author Justin Deoliveira, Boundless
 */
public class DatasetHandle extends Handle<Dataset> {

    Workspace parent;

    CoordinateReferenceSystem crs;
    Envelope bounds;

    public DatasetHandle(String name, Class<Dataset> type, Driver<?> driver, Workspace parent) {
        super(name, type, driver);
        this.parent = parent;
    }

    @Override
    public String title() throws IOException {
        if (title == null)  {
            title = resolve().getTitle();
        }
        return title;
    }

    @Override
    public String description() throws IOException {
        if (description == null) {
            description = resolve().getDescription();
        }
        return description;
    }

    public CoordinateReferenceSystem getCRS() {
        return crs;
    }

    public void setCRS(CoordinateReferenceSystem crs) {
        this.crs = crs;
    }

    public CoordinateReferenceSystem crs() throws IOException {
        if (crs == null ) {
           crs = resolve().crs();
        }
        return crs;
    }

    public Envelope getBounds() {
        return bounds;
    }

    public void setBounds(Envelope bounds) {
        this.bounds = bounds;
    }

    public Envelope bounds() throws IOException {
        if (bounds == null) {
            bounds = resolve().bounds();
        }
        return bounds;
    }

    @Override
    protected Dataset doResolve() throws IOException {
        return parent.get(name);
    }

}
