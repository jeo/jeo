package org.jeo.shp;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;

import org.jeo.data.Cursor;
import org.jeo.data.Vector;
import org.jeo.feature.Feature;
import org.jeo.feature.Field;
import org.jeo.feature.Schema;
import org.jeo.shp.dbf.DbaseFileHeader;
import org.jeo.shp.dbf.DbaseFileReader;
import org.jeo.shp.file.FileReader;
import org.jeo.shp.file.ShpFileType;
import org.jeo.shp.file.ShpFiles;
import org.jeo.shp.shp.IndexFile;
import org.jeo.shp.shp.ShapeType;
import org.jeo.shp.shp.ShapefileException;
import org.jeo.shp.shp.ShapefileHeader;
import org.jeo.shp.shp.ShapefileReader;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;

public class Shapefile implements Vector {

    ShpFiles shp;
    Schema schema;

    public Shapefile(File file) throws IOException {
        shp = new ShpFiles(file); 
        schema = readSchema();
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
    
            fields.add(new Field("geometry", geomType));
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
        //TODO, implement prj file reader
        return null;
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
    public long count(Envelope bbox) throws IOException {
        if (bbox == null) {
            return countAll();
        }

        ShapefileReader shpReader = new ShapefileReader(shp, false, false);
        try {
            long count = 0;
            while(shpReader.hasNext()) {
                shpReader.nextRecord();
                count++;
            }

            return count;
        }
        finally {
            shpReader.close();
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
    public Cursor<Feature> read(Envelope bbox) throws IOException {
        return new ShapefileCursor(this, bbox);
    }
    
    @Override
    public void add(Feature f) throws IOException {
        throw new UnsupportedOperationException();
    }

    ShapefileReader newShpReader() throws IOException {
        return new ShapefileReader(shp, false, false);
    }

    DbaseFileReader newDbfReader() throws IOException {
        return new DbaseFileReader(shp, false);
    }
}
