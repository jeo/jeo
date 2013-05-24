package org.jeo.shp;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.jeo.data.Cursor;
import org.jeo.data.Cursors;
import org.jeo.data.Disposable;
import org.jeo.data.Query;
import org.jeo.data.VectorData;
import org.jeo.feature.Feature;
import org.jeo.feature.Field;
import org.jeo.feature.ListFeature;
import org.jeo.feature.Schema;
import org.jeo.filter.Filter;
import org.jeo.geom.Geom;
import org.jeo.proj.Proj;
import org.jeo.shp.dbf.DbaseFileException;
import org.jeo.shp.dbf.DbaseFileHeader;
import org.jeo.shp.dbf.DbaseFileReader;
import org.jeo.shp.dbf.DbaseFileWriter;
import org.jeo.shp.file.FileReader;
import org.jeo.shp.file.ShpFileType;
import org.jeo.shp.file.ShpFiles;
import org.jeo.shp.file.StorageFile;
import org.jeo.shp.prj.PrjFileReader;
import org.jeo.shp.shp.IndexFile;
import org.jeo.shp.shp.ShapeType;
import org.jeo.shp.shp.ShapefileHeader;
import org.jeo.shp.shp.ShapefileReader;
import org.jeo.shp.shp.ShapefileWriter;
import org.jeo.shp.shp.ShapefileReader.Record;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;

public class ShpDataset implements VectorData, Disposable {

    public static ShpDataset create(File file, Schema schema) throws IOException {
        Field g = schema.geometry();
        if (g == null) {
            throw new IllegalArgumentException(
                "Unable to create shapefile from schema with no geometry");
        }

        final ShapeType shapeType;
        switch(Geom.Type.from(g.getType())) {
        case POINT:
            shapeType = ShapeType.POINT;
            break;
        case MULTIPOINT:
            shapeType = ShapeType.MULTIPOINT;
            break;
        case LINESTRING:
        case MULTILINESTRING:
            shapeType = ShapeType.ARC;
            break;
        case POLYGON:
        case MULTIPOLYGON:
            shapeType = ShapeType.POLYGON;
            break;
        default:
            throw new IllegalArgumentException(
                "Unable to create shapefile for geometry type " + g.getType().getSimpleName());
        }

        ShpFiles shpFiles = new ShpFiles(file);

        StorageFile shpStoragefile = shpFiles.getStorageFile(ShpFileType.SHP);
        StorageFile shxStoragefile = shpFiles.getStorageFile(ShpFileType.SHX);
        StorageFile dbfStoragefile = shpFiles.getStorageFile(ShpFileType.DBF);
        StorageFile prjStoragefile = shpFiles.getStorageFile(ShpFileType.PRJ);

        FileChannel shpChannel = shpStoragefile.getWriteChannel();
        FileChannel shxChannel = shxStoragefile.getWriteChannel();

        ShapefileWriter writer = new ShapefileWriter(shpChannel, shxChannel);
        try {
            // by spec, if the file is empty, the shape envelope should be ignored
            writer.writeHeaders(new Envelope(), shapeType, 0, 100);
        } finally {
            writer.close();
            assert !shpChannel.isOpen();
            assert !shxChannel.isOpen();
        }

        DbaseFileHeader dbfheader = createDbaseHeader(schema);
        dbfheader.setNumRecords(0);

        WritableByteChannel dbfChannel = dbfStoragefile.getWriteChannel();

        try {
            dbfheader.writeHeader(dbfChannel);
        } finally {
            dbfChannel.close();
        }

        CoordinateReferenceSystem crs = schema.crs();
        if (crs != null) {
            // .prj files should have no carriage returns in them, this
            // messes up
            // ESRI's ArcXXX software, so we'll be compatible
            String s = Proj.toWKT(crs, false);

            FileWriter prjWriter = new FileWriter(prjStoragefile.getFile());
            try {
                prjWriter.write(s);
            } finally {
                prjWriter.close();
            }
        } 

        StorageFile.replaceOriginals(shpStoragefile, shxStoragefile, dbfStoragefile, prjStoragefile);
        return new ShpDataset(file);
    }

    protected static DbaseFileHeader createDbaseHeader(Schema schema) 
        throws IOException, DbaseFileException {

        DbaseFileHeader header = new DbaseFileHeader();

        for (Field fld : schema) {
        
            Class<?> colType = fld.getType();
            String colName = fld.getName();

            int fieldLen = 255;
            
            if ((colType == Integer.class) || (colType == Short.class)
                    || (colType == Byte.class)) {
                header.addColumn(colName, 'N', Math.min(fieldLen, 9), 0);
            } else if (colType == Long.class) {
                header.addColumn(colName, 'N', Math.min(fieldLen, 19), 0);
            } else if (colType == BigInteger.class) {
                header.addColumn(colName, 'N', Math.min(fieldLen, 33), 0);
            } else if (Number.class.isAssignableFrom(colType)) {
                int l = Math.min(fieldLen, 33);
                int d = Math.max(l - 2, 0);
                header.addColumn(colName, 'N', l, d);
            // This check has to come before the Date one or it is never reached
            // also, this field is only activated with the following system property:
            // org.geotools.shapefile.datetime=true
            } /*else if (java.util.Date.class.isAssignableFrom(colType)
                       && Boolean.getBoolean("org.geotools.shapefile.datetime")) {
                header.addColumn(colName, '@', fieldLen, 0);
            }*/ else if (java.util.Date.class.isAssignableFrom(colType) ||
                    Calendar.class.isAssignableFrom(colType)) {
                header.addColumn(colName, 'D', fieldLen, 0);
            } else if (colType == Boolean.class) {
                header.addColumn(colName, 'L', 1, 0);
            } else if (CharSequence.class.isAssignableFrom(colType) || colType == java.util.UUID.class) {
                // Possible fix for GEOT-42 : ArcExplorer doesn't like 0 length
                // ensure that maxLength is at least 1
                header.addColumn(colName, 'C', Math.min(254, fieldLen), 0);
            } else if (Geometry.class.isAssignableFrom(colType)) {
                continue;
            } else {
                throw new IOException("Unable to write : " + colType.getName());
            }
        }

        return header;
    }
    
    ShpFiles shp;
    CoordinateReferenceSystem crs;
    Schema schema;

    public ShpDataset(File file) throws IOException {
        shp = new ShpFiles(file);
        crs = readCRS();
        schema = readSchema();
    }

    @Override
    public Shapefile getDriver() {
        return new Shapefile();
    }

    CoordinateReferenceSystem readCRS() throws IOException {
        PrjFileReader prj = newPrjReader();
        try {
            return prj.getCoodinateSystem();
        }
        finally {
            prj.close();
        }
    }

    Schema readSchema() throws IOException {
        List<Field> fields = new ArrayList<Field>();

        //read the geometry field
        ShapefileHeader shpHdr = shpHeader();
        ShapeType shpType = shpHdr.getShapeType();

        Class<? extends Geometry> geomType = Geometry.class;
        if (shpType.isPointType()) {
            geomType = Point.class;
        }
        else if (shpType.isMultiPointType()) {
            geomType = MultiPoint.class;
        }
        else if (shpType.isLineType()) {
            geomType = MultiLineString.class;
        }
        else if (shpType.isPolygonType()) {
            geomType = MultiPolygon.class;
        }

        fields.add(new Field("geometry", geomType, crs));

        //read the attribute fields
        DbaseFileHeader dbfHeader = dbfHeader();
        for (int i = 0; i < dbfHeader.getNumFields(); i++) {
            fields.add(new Field(dbfHeader.getFieldName(i), dbfHeader.getFieldClass(i)));
        }
        
        
        return new Schema(getName(), fields);
    }

    @Override
    public String getName() {
        return shp.getTypeName();
    }

    @Override
    public String getTitle() {
        return getName();
    }

    @Override
    public String getDescription() {
        return getDescription();
    }
    
    @Override
    public CoordinateReferenceSystem getCRS() {
        return crs;
    }

    @Override
    public Schema getSchema() throws IOException {
        return schema;
    }

    @Override
    public Envelope bounds() throws IOException {
        ReadableByteChannel in = null;
        try {
            ByteBuffer buffer = ByteBuffer.allocate(100);
            FileReader reader = new FileReader() {
                public String id() {
                    return "Shapefile Datastore's getBounds Method";
                }
            };

            in = shp.getReadChannel(ShpFileType.SHP, reader);
            try {
                in.read(buffer);
                buffer.flip();

                ShapefileHeader hdr = new ShapefileHeader();
                hdr.read(buffer, true);

                return new Envelope(hdr.minX(), hdr.maxX(), hdr.minY(), hdr.maxY());

            } finally {
                in.close();
            }
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ioe) {
                // do nothing
            }
        } 
    }

    @Override
    public long count(Query q) throws IOException {
        //try to optimize the count
        if (q == null || q.isAll()) {
            return countAll();
        }

        if (Geom.isNull(q.getBounds()) && Filter.isTrueOrNull(q.get(Query.FILTER))) {
            //count all and apply limit/offset
            long count = countAll();

            Integer offset = q.consume(Query.OFFSET, 0);
            Integer limit = q.consume(Query.LIMIT, (int) count);

            count = Math.max(0, count - offset);
            count = Math.min(count, limit);

            return count;
        }
        else {
            //load and count the full cursor
            return Cursors.size(cursor(q));
        }
    }

    long countAll() throws IOException {
        IndexFile file = openIdxFile();
        if (file != null) {
            try {
                return file.getRecordCount();
            } finally {
                file.close();
            }
        }

        // no Index file so use the number of shapefile records
        ShapefileReader shpReader = new ShapefileReader(shp, false, false);
        int count = -1;

        try {
            count = shpReader.getCount(count);
        } catch (IOException e) {
            throw e;
        } finally {
            shpReader.close();
        }

        return count;

    }

    IndexFile openIdxFile() {
        if (shp.get(ShpFileType.SHX) == null) {
            return null;
        }

        if (shp.isLocal() && !shp.exists(ShpFileType.SHX)) {
            return null;
        }

        try {
            return new IndexFile(shp, false);
        } catch (IOException e) {
            // could happen if shx file does not exist remotely
            return null;
        }
    }

    @Override
    public Cursor<Feature> cursor(Query q) throws IOException {
        if (q.getMode() == Cursor.APPEND) {
            return new ShpAppendCursor(this);
        }

        Cursor<Feature> cursor = q.getMode() == Cursor.READ ? 
            new ShpCursor(this, q.getBounds()) : new ShpUpdateCursor(this, q.getBounds());

        //if (q.isAll()) {
        //    return new ShpCursor(this, null);
        //}

        //TODO: q.getProperties()
        return q.apply(cursor);
    }

    public void dispose() {
        if (shp != null) {
            shp.dispose();
        }
        shp = null;
    }

    protected void finalize() throws Throwable {
        dispose();
    }

    ShpFiles getShp() {
        return shp;
    }

    ShapefileReader newShpReader() throws IOException {
        return new ShapefileReader(shp, false, false);
    }

    ShapefileWriter newShpWriter() throws IOException {
        FileChannel shpChannel = shp.getStorageFile(ShpFileType.SHP).getWriteChannel();
        FileChannel shxChannel = shp.getStorageFile(ShpFileType.SHX).getWriteChannel();
        
        return new ShapefileWriter(shpChannel, shxChannel);
    }

    ShapefileHeader shpHeader() throws IOException {
        ShapefileReader shpReader = new ShapefileReader(shp, false, false);
        try {
            return shpReader.getHeader();
        }
        finally {
            shpReader.close();
        }
    }

    DbaseFileReader newDbfReader() throws IOException {
        return new DbaseFileReader(shp, false);
    }

    DbaseFileWriter newDbfWriter() throws IOException {
        return new DbaseFileWriter(dbfHeader(), shp.getStorageFile(ShpFileType.DBF).getWriteChannel());
    }

    DbaseFileHeader dbfHeader() throws IOException {
        DbaseFileReader dbfReader = new DbaseFileReader(shp, false);
        try {
            return dbfReader.getHeader();
        }
        finally {
            dbfReader.close();
        }
    }

    PrjFileReader newPrjReader() throws IOException {
        return new PrjFileReader(shp);
    }

    Feature next(ShapefileReader shpReader, DbaseFileReader dbfReader, Envelope bbox) 
            throws IOException {

        if (shpReader.hasNext()) {
            //read next record and check if it intersects
            Record r = shpReader.nextRecord();
            if (bbox == null || bbox.intersects(r.envelope())) {

                List<Object> values = new ArrayList<Object>();
                values.add(r.shape());
                
                if (dbfReader.hasNext()) {
                    for (Object o : dbfReader.readEntry()) {
                        values.add(o);
                    }
                }

                return new ListFeature(String.valueOf(r.number), values, getSchema());
            }
            else {
                //does not intersect, skip the dbf row as well
                if (dbfReader.hasNext()) {
                    dbfReader.skip();
                }
            }
        }

        return null;
    }
}
