package org.jeo.geojson;

import java.io.IOException;

import org.json.simple.parser.ParseException;
import org.osgeo.proj4j.CoordinateReferenceSystem;

public class CRSFinder extends BaseHandler {

    CoordinateReferenceSystem crs;

    public CoordinateReferenceSystem getCRS() {
        return crs;
    }

    @Override
    public void startJSON() throws ParseException, IOException {
    }
    
    @Override
    public void endJSON() throws ParseException, IOException {
    }
    
    @Override
    public boolean startObject() throws ParseException, IOException {
        return true;
    }
    
    @Override
    public boolean endObject() throws ParseException, IOException {
        return true;
    }
    
    @Override
    public boolean startObjectEntry(String key) throws ParseException, IOException {
        if ("crs".equals(key)) {
            push(key, new CRSHandler() {
               @Override
                public boolean endObject() throws ParseException, IOException {
                   super.endObject();
                   crs = (CoordinateReferenceSystem) node.getValue();
                   return false;
                } 
            });
        }
        return true;
    }
    
    @Override
    public boolean endObjectEntry() throws ParseException, IOException {
        return true;
    }
    
    @Override
    public boolean startArray() throws ParseException, IOException {
        return true;
    }
    
    @Override
    public boolean endArray() throws ParseException, IOException {
        return true;
    }
    
    @Override
    public boolean primitive(Object value) throws ParseException, IOException {
        return true;
    }

}
