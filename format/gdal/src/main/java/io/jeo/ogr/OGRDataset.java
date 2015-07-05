/* Copyright 2014 The jeo project. All rights reserved.
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
package io.jeo.ogr;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

import io.jeo.vector.FeatureAppendCursor;
import io.jeo.vector.FeatureCursor;
import io.jeo.vector.FeatureWriteCursor;
import org.gdal.ogr.DataSource;
import org.gdal.ogr.FeatureDefn;
import org.gdal.ogr.FieldDefn;
import org.gdal.ogr.Layer;
import org.gdal.osr.SpatialReference;
import io.jeo.data.Driver;
import io.jeo.data.FileData;
import io.jeo.vector.VectorQuery;
import io.jeo.vector.VectorQueryPlan;
import io.jeo.vector.VectorDataset;
import io.jeo.vector.Schema;
import io.jeo.vector.SchemaBuilder;
import io.jeo.geom.Envelopes;
import io.jeo.proj.Proj;
import io.jeo.util.Key;
import io.jeo.util.Pair;
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

import static org.gdal.ogr.ogrConstants.*;

public class OGRDataset implements VectorDataset, FileData {

    public static final Logger LOG = LoggerFactory.getLogger(OGR.class);

    String name;
    OGRWorkspace workspace;

    public OGRDataset(String name, OGRWorkspace workspace) {
        this.name = name;
        this.workspace = workspace;
    }

    @Override
    public File file() {
        return workspace.file();
    }

    @Override
    public Driver<?> driver() {
        return workspace.driver();
    }

    @Override
    public Map<Key<?>, Object> driverOptions() {
        return workspace.driverOptions();
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Schema schema() throws IOException {
        Pair<Layer,DataSource> data = open();
        try {
            return toSchema(data.first);
            
        }
        finally {
            close(data);
        }
    }

    Schema toSchema(Layer l) {

        FeatureDefn defn = l.GetLayerDefn();

        SchemaBuilder sb = Schema.build(defn.GetName());

        Class<? extends Geometry> geotype = toGeometryType(defn);
        if (geotype != null) {
            CoordinateReferenceSystem crs = toCRS(l);
            sb.field("geometry", geotype, crs);
        }

        for (int i = 0; i < defn.GetFieldCount(); i++) {
            FieldDefn fd = defn.GetFieldDefn(i);
            sb.field(fd.GetName(), toType(fd));
        }

        return sb.schema();
    }

    CoordinateReferenceSystem toCRS(Layer l) {
        SpatialReference sref = l.GetSpatialRef();
        if (sref != null) {
            return Proj.crs(sref.ExportToProj4());
        }
        return null;
    }

    Class<? extends Geometry> toGeometryType(FeatureDefn defn) {
        int g = defn.GetGeomType();

        if (g == wkbNone) {
            return null;
        }
        if (g == wkbPoint || g == wkbPoint25D) {
            return Point.class;
        } 
        if (g == wkbLinearRing) {
            return LinearRing.class;
        } 
        if (g == wkbLineString || g == wkbLineString25D
            || g == wkbMultiLineString || g == wkbMultiLineString25D) {
            return MultiLineString.class;
        } 
        if (g == wkbPolygon || g == wkbPolygon25D
                || g == wkbMultiPolygon || g == wkbMultiPolygon25D) {
            return MultiPolygon.class;
        } 
        if (g == wkbGeometryCollection || g == wkbGeometryCollection25D) {
            return GeometryCollection.class;
        }
        if (g == wkbUnknown) {
            return Geometry.class;
        }

        LOG.debug("unknown ogr geometry type: " + g);
        return null;
    }

    Class<?> toType(FieldDefn defn) {
        int type = defn.GetFieldType();
        int width = defn.GetWidth();

        if (type == OFTInteger) {
            if (width <= 3) {
                return Byte.class;
            } else if (width <= 5) {
                return Short.class;
            } else if (width <= 9) {
                return Integer.class;
            } else if (width <= 19) {
                return Long.class;
            } else {
                return BigDecimal.class;
            }
        }
        
        if (type == OFTIntegerList) {
            return int[].class;
        }

        if (type == OFTReal) {
            if (width <= 12) {
                return Float.class;
            } else if (width <= 22) {
                return Double.class;
            } else {
                return BigDecimal.class;
            }
        }

        if (type == OFTRealList) {
            return double[].class;
        }

        if (type == OFTBinary) {
            return byte[].class;
        }

        if (type == OFTDate) {
            return java.sql.Date.class;
        } 

        if (type == OFTTime) {
            return java.sql.Time.class;
        }

        if (type == OFTDateTime) {
            return java.sql.Timestamp.class;
        }

        if (type == OFTString) {
            return String.class;
        }

        if (type == OFTStringList) {
            return String[].class;
        }

        LOG.debug("unknown field type:" + type);
        return String.class;
    }

    @Override
    public CoordinateReferenceSystem crs() throws IOException {
        Pair<Layer,DataSource> data = open();
        try {
            SpatialReference sref = data.first.GetSpatialRef();
            if (sref == null) {
                return null;
            }

            try {
                // have ogr try to map random projection into a well known code
                sref.AutoIdentifyEPSG();
            }
            catch(Exception e) {
                LOG.debug("error auto identifying epsg code", e);
            }

            CoordinateReferenceSystem crs = null;

            // first try by mapping directly epsg code
            String code = sref.GetAuthorityCode(null);
            if (code != null) {
                crs = Proj.crs("epsg:" + code);
            }

            if (crs == null) {
                // use proj string
                crs = Proj.crs(sref.ExportToProj4());
            }

            return crs;
        }
        finally {
            close(data);
        }
    }
    
    @Override
    public Envelope bounds() throws IOException {
        Pair<Layer,DataSource> data = open();
        try {
            double[] d = data.first.GetExtent(true);
            return new Envelope(d[0], d[1], d[2], d[3]);
        }
        catch(Exception e) {
            throw new IOException("Error calculating bounds", e);
        }
        finally {
            close(data);
        }
    }

    @Override
    public long count(VectorQuery q) throws IOException {

        Pair<Layer,DataSource> data = open();
        try {
            Layer l = data.first;

            if (!Envelopes.isNull(q.bounds())) {
                Envelope bb = q.bounds();
                l.SetSpatialFilterRect(bb.getMinX(), bb.getMinY(), bb.getMaxX(), bb.getMaxY());
            }

            if (!q.isFiltered()) {
                return q.adjustCount(l.GetFeatureCount());
            }

            //TODO: convert attribute filter to ogr sql
            return read(q).count();

        }
        catch(Exception e) {
            throw new IOException("Error calculating count", e);
        }
        finally {
            close(data);
        }
    }

    @Override
    public FeatureCursor read(VectorQuery q) throws IOException {
        Pair<Layer,DataSource> data = open();

        Layer l = data.first;

        VectorQueryPlan qp = new VectorQueryPlan(q);
        if (!Envelopes.isNull(q.bounds())) {
            Envelope bb = q.bounds();
            l.SetSpatialFilterRect(bb.getMinX(), bb.getMinY(), bb.getMaxX(), bb.getMaxY());
            qp.bounded();
        }

        return qp.apply(new OGRCursor(l, data.second, this));
    }

    @Override
    public FeatureWriteCursor update(VectorQuery q) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public FeatureAppendCursor append(VectorQuery q) throws IOException {
        throw new UnsupportedOperationException();
    }

    Pair<Layer,DataSource> open() throws IOException {
        DataSource ds = workspace.open();
        return Pair.of(ds.GetLayer(0), ds);
    }

    void close(Pair<Layer,DataSource> data) {
        data.first.delete();
        data.second.delete();
    }

    @Override
    public void close() {
    }
}
