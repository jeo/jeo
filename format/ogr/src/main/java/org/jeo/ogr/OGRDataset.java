package org.jeo.ogr;

import java.io.IOException;
import java.util.Date;

import org.gdal.ogr.DataSource;
import org.gdal.ogr.FeatureDefn;
import org.gdal.ogr.FieldDefn;
import org.gdal.ogr.Layer;
import org.gdal.ogr.ogrConstants;
import org.jeo.data.Cursor;
import org.jeo.data.Driver;
import org.jeo.data.Query;
import org.jeo.data.VectorData;
import org.jeo.feature.Feature;
import org.jeo.feature.Schema;
import org.jeo.feature.SchemaBuilder;
import org.jeo.geom.Geom;
import org.jeo.proj.Proj;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;

public class OGRDataset implements VectorData {

    static Logger LOG = LoggerFactory.getLogger(OGRDriver.class);

    DataSource data;
    OGRDriver<?> driver;

    public OGRDataset(DataSource data, OGRDriver<?> driver) {
        this.data = data;
        this.driver = driver;
    }

    @Override
    public Driver<?> getDriver() {
        return driver;
    }

    @Override
    public String getName() {
        return layer().GetName();
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public Schema getSchema() throws IOException {
        FeatureDefn def = layer().GetLayerDefn();
        SchemaBuilder sb = new SchemaBuilder(getName());

        Class<? extends Geometry> geometryType = gtype(def);
        if (geometryType != null) {
            sb.field("geometry", geometryType, getCRS());
        }

        //Class<? extends Geometry> gtype = 
        for (int i = 0; i < def.GetFieldCount(); i++) {
            FieldDefn field = def.GetFieldDefn(i);
            sb.field(field.GetName(), type(field));
        }

        return sb.schema();
    }

    Class<?> type(FieldDefn f) {
        int type = f.GetFieldType();
        if (type == ogrConstants.OFTInteger) {
            return Integer.class;
        }
        if (type == ogrConstants.OFTReal) {
            return Double.class;
        }
        if (type == ogrConstants.OFTString) {
            return String.class;
        }
        if (type == ogrConstants.OFTDate || type == ogrConstants.OFTDateTime) {
            return Date.class;
        }
        if (type == ogrConstants.OFTBinary) {
            return byte[].class;
        }

        LOG.debug("Unknown type: " + type + ", falling back to Object");
        return Object.class;
    }

    Class<? extends Geometry> gtype(FeatureDefn f) {
        int t = f.GetGeomType();

        if (t == ogrConstants.wkbPoint || t == ogrConstants.wkbPoint25D) {
            return Point.class;
        } 
        else if (t == ogrConstants.wkbLinearRing) {
            return LinearRing.class;
        } 
        else if (t == ogrConstants.wkbLineString
            || t == ogrConstants.wkbLineString25D
            || t == ogrConstants.wkbMultiLineString
            || t == ogrConstants.wkbMultiLineString25D) {
            return MultiLineString.class;
        } 
        else if (t == ogrConstants.wkbPolygon
            || t == ogrConstants.wkbPolygon25D
            || t == ogrConstants.wkbMultiPolygon
            || t == ogrConstants.wkbMultiPolygon25D) {
            return MultiPolygon.class;
        } 
        else if (t == ogrConstants.wkbGeometryCollection
            || t == ogrConstants.wkbGeometryCollection25D) {
            return GeometryCollection.class;
        } 
        else if (t == ogrConstants.wkbNone) {
            return null;
        } 
        else if (t == ogrConstants.wkbUnknown) {
            return Geometry.class;
        } 
        else {
            throw new IllegalArgumentException("Unknown geometry type: " + t);
        }
    }

    @Override
    public CoordinateReferenceSystem getCRS() throws IOException {
        String projdef = layer().GetSpatialRef().ExportToProj4();
        return projdef != null ? Proj.crs(projdef.split("\\s+")) : null;
    }

    @Override
    public Envelope bounds() throws IOException {
        double[] ext = layer().GetExtent();
        return new Envelope(ext[0], ext[2], ext[1], ext[3]);
    }

    @Override
    public void dispose() {
        if (data != null) {
            data.delete();
        }
        data = null;
    }

    @Override
    public long count(Query q) throws IOException {
        Layer layer = layer();
        
        if (!Geom.isNull(q.getBounds())) {
            layer.SetSpatialFilter(
                org.gdal.ogr.Geometry.CreateFromWkt(Geom.toPolygon(q.getBounds()).toText()));
        }

        return layer.GetFeatureCount();
    }

    @Override
    public Cursor<Feature> cursor(Query q) throws IOException {
        Layer layer = layer();

        if (!Geom.isNull(q.getBounds())) {
            layer.SetSpatialFilter(
                org.gdal.ogr.Geometry.CreateFromWkt(Geom.toPolygon(q.getBounds()).toText()));
        }

        return q.apply(new OGRCursor(layer, this));
    }

    Layer layer() {
        return data.GetLayer(0);
    }
}
