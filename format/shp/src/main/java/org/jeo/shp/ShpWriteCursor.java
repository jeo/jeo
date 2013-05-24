package org.jeo.shp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jeo.data.Cursor;
import org.jeo.feature.Feature;
import org.jeo.feature.ListFeature;
import org.jeo.shp.dbf.DbaseFileHeader;
import org.jeo.shp.dbf.DbaseFileWriter;
import org.jeo.shp.file.ShpFileType;
import org.jeo.shp.file.StorageFile;
import org.jeo.shp.shp.ShapeHandler;
import org.jeo.shp.shp.ShapefileHeader;
import org.jeo.shp.shp.ShapefileReader;
import org.jeo.shp.shp.ShapefileWriter;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

public abstract class ShpWriteCursor extends Cursor<Feature> {

    protected ShpDataset shp;

    /*
     * temporary storage files that we actually write to
     */
    protected StorageFile shpFile, shxFile, dbfFile;

    /*
     * shp and dbf writers 
     */
    protected ShapefileWriter shpWriter;
    protected DbaseFileWriter dbfWriter;

    /*
     * number of records written 
     */
    protected int records;
    /*
     * total file length 
     */
    protected int shplen;
    /*
     * bounds of geometries written 
     */
    protected Envelope bounds;

    /*
     * handler use to track geometry length 
     */
    protected ShapeHandler shpHandler;
    
    /*
     * current feature being written
     */
    protected Feature next;

    ShpWriteCursor(ShpDataset shp, Mode mode) throws IOException {
        super(mode);

        this.shp = shp;

        // initialize house keeping variables
        records = 0;
        shplen = 100;
        bounds = new Envelope();

        // create temporary files
        shpFile = shp.getShp().getStorageFile(ShpFileType.SHP);
        shxFile = shp.getShp().getStorageFile(ShpFileType.SHX);
        dbfFile = shp.getShp().getStorageFile(ShpFileType.DBF);

        ShapefileHeader shpHeader = shp.shpHeader();

        //create the shp and write out an empty header 
        shpWriter = new ShapefileWriter(shpFile.getWriteChannel(), shxFile.getWriteChannel());
        shpWriter.writeHeaders(bounds, shpHeader.getShapeType(), records, shplen);

        // create dbf writer
        dbfWriter = new DbaseFileWriter(shp.dbfHeader(), dbfFile.getWriteChannel());

        shpHandler = shpHeader.getShapeType().getShapeHandler(new GeometryFactory());    
    }

    @Override
    public void close() throws IOException {
        //write the headers back
        shpWriter.writeHeaders(bounds, shp.shpHeader().getShapeType(), records, shplen);
        shpWriter.close();

        DbaseFileHeader dbfHeader = shp.dbfHeader();
        dbfHeader.setNumRecords(records);

        dbfFile.getWriteChannel().position(0);
        dbfHeader.writeHeader(dbfFile.getWriteChannel());
        
        dbfWriter.close();
        StorageFile.replaceOriginals(shpFile, shxFile, dbfFile);

    }

    @Override
    protected void doWrite() throws IOException {
        Geometry g = next.geometry();
        if (g != null) {
            bounds.expandToInclude(g.getEnvelopeInternal());
        }

        shplen += g != null ? shpHandler.getLength(g) + 8 : 4 + 8;
        records++;
        
        shpWriter.writeGeometry(next.geometry());

        List<Object> record = new ArrayList<Object>();
        for (Object obj : next.list()) {
            if (obj instanceof Geometry) {
                continue;
            }
            record.add(obj);
        }
        
        dbfWriter.write(record.toArray(new Object[record.size()]));
        next = null;
    }

}
