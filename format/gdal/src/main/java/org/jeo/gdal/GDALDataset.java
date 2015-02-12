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
package org.jeo.gdal;

import com.vividsolutions.jts.geom.Envelope;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.osr.SpatialReference;
import org.jeo.data.Driver;
import org.jeo.data.FileData;
import org.jeo.raster.RasterDataset;
import org.jeo.raster.RasterQuery;
import org.jeo.proj.Proj;
import org.jeo.raster.*;
import org.jeo.util.*;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.gdal.gdalconst.gdalconstConstants.*;

public class GDALDataset implements RasterDataset, FileData {

    Dataset dataset;
    File file;
    GDAL driver;

    public GDALDataset(File file, Dataset dataset, GDAL driver) {
        this.file = file;
        this.dataset = dataset;
        this.driver = driver;
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public String getName() {
        return Util.base(file.getName());
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public String getDescription() {
        return dataset.GetDescription();
    }

    @Override
    public Driver<?> getDriver() {
        return driver;
    }

    @Override
    public Map<Key<?>, Object> getDriverOptions() {
        LinkedHashMap<Key<?>, Object> opts = new LinkedHashMap<Key<?>, Object>();
        opts.put(GDAL.FILE, file);
        return opts;
    }

    @Override
    public CoordinateReferenceSystem crs() throws IOException {
        String proj = dataset.GetProjection();
        if (proj != null) {
            SpatialReference ref = new SpatialReference(proj);
            return Proj.crs(ref.ExportToProj4());
        }
        return null;
    }

    @Override
    public Envelope bounds() throws IOException {
        return bounds(dataset);
    }

    Envelope bounds(Dataset dataset) {
        Dimension size = size(dataset);
        double[] tx = dataset.GetGeoTransform();
        return new Envelope(tx[0], tx[0] + size.width() * tx[1],
                tx[3], tx[3] + size.width()*tx[4] + size.height()*tx[5]);
    }

    @Override
    public Dimension size() {
        return size(dataset);
    }

    Dimension size(Dataset dataset) {
        return new Dimension(dataset.getRasterXSize(), dataset.getRasterYSize());
    }

    public Rect rect() {
        return rect(dataset);
    }

    Rect rect(Dataset dataset) {
        Dimension size = size(dataset);
        return new Rect(0, 0, size.width(), size.height());
    }

    @Override
    public List<Band> bands() throws IOException {
        int nbands = dataset.GetRasterCount();

        List<Band> bands = new ArrayList<Band>(nbands);
        for (int i = 1; i <= nbands; i++) {
            bands.add(new GDALBand(dataset.GetRasterBand(i)));
        }

        return bands;
    }

    @Override
    public Raster read(RasterQuery query) throws IOException {
        Raster raster = new Raster();

        Dataset data = dataset;

        // reprojection
        raster.crs(crs());
        if (query.crs() != null && !Proj.equal(query.crs(), crs())) {
            String srcWkt = toWKT(crs());
            String dstWkt = toWKT(query.crs());

            //TODO: allow query to specify interpolation method
            data = gdal.AutoCreateWarpedVRT(dataset, srcWkt, dstWkt);
            raster.crs(query.crs());
        }

        // area of raster to load
        Rect r = rect(data);            // raster space
        Envelope bbox = bounds(data);   // world space
        if (query.getBounds() != null) {
            // intersect bounds with query bounds
            Envelope i = bbox.intersection(query.getBounds());
            r = r.map(i, bbox);
            bbox = i;
        }
        raster.bounds(bbox);

        // raster size
        Dimension s = query.getSize();
        if (s == null) {
            s = size(data);
        }
        raster.size(s);

        // band selection
        List<GDALBand> queryBands = bands(query.getBands());
        int[] bands = new int[queryBands.size()];
        for (int i = 0 ; i < queryBands.size(); i++) {
            GDALBand band = queryBands.get(i);
            bands[i] = band.index();
        }
        raster.bands((List)queryBands);

        // figure out the buffer type if not specified
        DataType datatype = query.getDataType();
        if (datatype == null) {
            datatype = DataType.BYTE;
            for (int i = 0 ; i < queryBands.size(); i++) {
                GDALBand band = queryBands.get(i);
                DataType dt = band.datatype();
                if (dt.compareTo(datatype) > 0) {
                    datatype = dt;
                }
            }
        }

        ByteBuffer buffer = ByteBuffer.allocateDirect(s.width()*s.height()*datatype.size());
        buffer.order(ByteOrder.nativeOrder());

        if (bands.length == 1) {
            // single band, read in same units as requested buffer
            data.ReadRaster_Direct(r.left, r.top, r.width(), r.height(), s.width(), s.height(),
                toGDAL(datatype), buffer, bands, 0, 0, 0);
        }
        else {
            // multi band mode, read as byte and back into buffer
            data.ReadRaster_Direct(r.left, r.top, r.width(), r.height(), s.width(), s.height(),
                GDT_Byte, buffer, bands, datatype.size(), 0, 1);
        }

        return raster.data(DataBuffer.create(buffer, datatype));
    }

    int toGDAL(DataType datatype) {
        switch(datatype) {
            case CHAR:
                break;
            case BYTE:
                return GDT_Byte;
            case SHORT:
                return GDT_Int16;
            case INT:
                return GDT_Int32;
            case LONG:
                return GDT_UInt32;
            case FLOAT:
                return GDT_Float32;
            case DOUBLE:
                return GDT_Float64;
        }
        throw new IllegalArgumentException("unsupported data type: " + datatype);
    }

    String toWKT(CoordinateReferenceSystem crs) {
        SpatialReference ref = new SpatialReference();
        ref.ImportFromProj4(Proj.toString(crs));
        return ref.ExportToWkt();
    }

    List<GDALBand> bands(int[] bands) throws IOException {
        List<Band> allBands = bands();

        if (bands == null || bands.length == 0) {
            return (List) allBands;
        }

        List<GDALBand> list = new ArrayList(bands.length);
        for (int b : bands) {
            list.add((GDALBand) allBands.get(b));
        }

        return list;
    }

    @Override
    public void close() {
        if (dataset != null) {
            dataset.delete();
            dataset = null;
        }
    }

    static class GDALBand implements Band {

        org.gdal.gdal.Band band;

        GDALBand(org.gdal.gdal.Band band) {
            this.band = band;
        }

        @Override
        public String name() {
            return band.GetDescription();
        }

        int index() {
            return band.GetBand();
        }

        @Override
        public Color color() {
            int ci = band.GetColorInterpretation();
            return ci == GCI_Undefined ? Color.UNDEFINED :
                   ci == GCI_GrayIndex ? Color.GRAY :
                   ci == GCI_RedBand ? Color.RED :
                   ci == GCI_GreenBand ? Color.GREEN :
                   ci == GCI_BlueBand ? Color.BLUE : Color.GRAY;
        }

        @Override
        public DataType datatype() {
            int dt = band.GetRasterDataType();
            return dt == GDT_Byte ? DataType.BYTE :
                   dt == GDT_Int16 ? DataType.SHORT :
                   dt == GDT_UInt16 ? DataType.INT :
                   dt == GDT_Int32 ?  DataType.INT :
                   dt == GDT_UInt32 ? DataType.LONG :
                   dt == GDT_Float32 ? DataType.FLOAT :
                   dt == GDT_Float64 ? DataType.DOUBLE :
                   null;

            //TODO
            //GDT_CInt16
            //GDT_CInt32
            //GDT_CFloat32
            //GDT_CFloat64
        }

        @Override
        public Double nodata() {
            Double[] nodata = new Double[]{null};
            band.GetNoDataValue(nodata);
            return nodata[0];
        }

        @Override
        public Stats stats() throws IOException {
            Stats stats = new Stats();

            double[] arr = new double[2];
            band.ComputeRasterMinMax(arr);
            stats.min(arr[0]).max(arr[1]);

            band.ComputeBandStats(arr);
            stats.mean(arr[0]).stdev(arr[1]);

            return stats;
        }
    }
}
