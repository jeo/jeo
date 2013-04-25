package org.jeo.shp;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;

import org.jeo.data.Cursor;
import org.jeo.data.Cursors;
import org.jeo.data.Disposable;
import org.jeo.data.Query;
import org.jeo.data.Vector;
import org.jeo.feature.Feature;
import org.jeo.feature.Field;
import org.jeo.feature.Schema;
import org.jeo.filter.Filter;
import org.jeo.geom.Geom;
import org.jeo.shp.dbf.DbaseFileHeader;
import org.jeo.shp.dbf.DbaseFileReader;
import org.jeo.shp.file.FileReader;
import org.jeo.shp.file.ShpFileType;
import org.jeo.shp.file.ShpFiles;
import org.jeo.shp.prj.PrjFileReader;
import org.jeo.shp.shp.IndexFile;
import org.jeo.shp.shp.ShapeType;
import org.jeo.shp.shp.ShapefileHeader;
import org.jeo.shp.shp.ShapefileReader;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;

public class ShpDataset implements Vector, Disposable {

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
        ShapefileReader shpReader = new ShapefileReader(shp, false, false);
        try {
            ShapefileHeader shpHdr = shpReader.getHeader();
            ShapeType shpType = shpHdr.getShapeType();
    
            Class<? extends Geometry> geomType = Geometry.class;
            if (shpType.isPointType()) {
                geomType = Point.class;
            }
            else if (shpType.isMultiPoint() || shpType.isMultiPointType()) {
                geomType = MultiPoint.class;
            }
            else if (shpType.isLineType()) {
                geomType = MultiLineString.class;
            }
            else if (shpType.isPolygonType()) {
                geomType = MultiPolygon.class;
            }
    
            fields.add(new Field("geometry", geomType, crs));
        } finally {
            shpReader.close();
        }

        //read the attribute fields
        DbaseFileReader dbfReader = new DbaseFileReader(shp, false);
        try {
            DbaseFileHeader dbfHeader = dbfReader.getHeader();
            
            for (int i = 0; i < dbfHeader.getNumFields(); i++) {
                
                fields.add(new Field(dbfHeader.getFieldName(i), dbfHeader.getFieldClass(i)));
            }
        }
        finally {
            dbfReader.close();
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
        if (q.getMode() != Cursor.READ) {
            throw new IllegalArgumentException("Write cursor not supported"); 
        }
        if (q.isAll()) {
            return new ShpCursor(this, null);
        }

        //TODO: q.getProperties()
        return q.apply(new ShpCursor(this, q.getBounds()));
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

    ShapefileReader newShpReader() throws IOException {
        return new ShapefileReader(shp, false, false);
    }

    DbaseFileReader newDbfReader() throws IOException {
        return new DbaseFileReader(shp, false);
    }

    PrjFileReader newPrjReader() throws IOException {
        return new PrjFileReader(shp);
    }
}
