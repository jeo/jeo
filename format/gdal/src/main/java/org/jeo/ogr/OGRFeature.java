package org.jeo.ogr;

import static org.gdal.ogr.ogrConstants.OFTBinary;
import static org.gdal.ogr.ogrConstants.OFTDate;
import static org.gdal.ogr.ogrConstants.OFTDateTime;
import static org.gdal.ogr.ogrConstants.OFTInteger;
import static org.gdal.ogr.ogrConstants.OFTIntegerList;
import static org.gdal.ogr.ogrConstants.OFTReal;
import static org.gdal.ogr.ogrConstants.OFTRealList;
import static org.gdal.ogr.ogrConstants.OFTString;
import static org.gdal.ogr.ogrConstants.OFTStringList;
import static org.gdal.ogr.ogrConstants.OFTTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.gdal.ogr.Feature;
import org.jeo.feature.BasicFeature;
import org.jeo.feature.Field;
import org.jeo.feature.Schema;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;

public class OGRFeature extends BasicFeature {

    public OGRFeature(Feature f, Schema schema) {
        super(String.valueOf(f.GetFID()), new OGRFeatureStorage(f, schema));
    }
    
    static class OGRFeatureStorage extends BasicFeature.Storage {

        Feature f;

        OGRFeatureStorage(Feature f, Schema schema) {
            super(schema);
            this.f = f;
        }

        @Override
        protected Schema buildSchema() {
            return null;
        }
    
        @Override
        protected Geometry findGeometry() {
            return geo();
        }
    
        Geometry geo() {
            org.gdal.ogr.Geometry g = f.GetGeometryRef();
            if (g == null) {
                return null;
            }
    
            try {
                return new WKBReader().read(g.ExportToWkb());
            } catch (ParseException e) {
                throw new RuntimeException("Unable to parse geometry wkb");
            }
        }

        @Override
        protected Object get(String key) {
            return "geometry".equals(key) ? geo() : raw(f.GetFieldIndex(key)); 
        }
    
        @Override
        protected Object get(int index) {
            return raw(mapIndex(index));
        }

        int mapIndex(int i) {
            return schema().geometry() == null ? i : i - 1;
        }

        int unmapIndex(int i) {
            return schema().geometry() == null ? i : i + 1;
        }

        protected Object raw(int i) {
            if (i < 0) {
                return null;
            }

            int type = f.GetFieldType(i);
            if (type == OFTInteger) {
                return f.GetFieldAsInteger(i);
            }
            
            if (type == OFTIntegerList) {
                return f.GetFieldAsIntegerList(i);
            }
    
            if (type == OFTReal) {
                return f.GetFieldAsDouble(i);
            }
    
            if (type == OFTRealList) {
                return f.GetFieldAsDoubleList(i);
            }
    
            if (type == OFTBinary) {
                return byte[].class;
            }
    
            if (type == OFTDate || type == OFTTime || type == OFTDateTime) {
                return temporal(i);
            } 

            if (type == OFTString) {
                return f.GetFieldAsString(i);
            }
    
            if (type == OFTStringList) {
                return f.GetFieldAsStringList(i);
            }

            //TODO: log
            return null;
        }

        protected java.util.Date temporal(int i) {

            int[] year = new int[1];
            int[] month = new int[1];
            int[] day = new int[1];
            int[] hour = new int[1];
            int[] minute = new int[1];
            int[] second = new int[1];
            int[] timeZone = new int[1];

            f.GetFieldAsDateTime(i, year, month, day, hour, minute, second, timeZone);

            Calendar cal = Calendar.getInstance();

            // from ogr_core.h 
            // 0=unknown, 1=localtime(ambiguous), 100=GMT, 104=GMT+1, 80=GMT-5, etc
            int tz = timeZone[0];
            if(tz != 0 && tz != 1) {
                int offset = tz - 100 / 4;
                if(offset < 0) {
                    cal.setTimeZone(TimeZone.getTimeZone("GMT" + offset));
                } else if(offset == 0) {
                    cal.setTimeZone(TimeZone.getTimeZone("GMT"));
                } else {
                    cal.setTimeZone(TimeZone.getTimeZone("GMT+" + offset));
                }
            }
            cal.clear();
            cal.set(Calendar.YEAR, year[0]);
            cal.set(Calendar.MONTH, month[0]);
            cal.set(Calendar.DAY_OF_MONTH, day[0]);
            cal.set(Calendar.HOUR_OF_DAY, hour[0]);
            cal.set(Calendar.MINUTE, minute[0]);
            cal.set(Calendar.SECOND, second[0]);

            Field fld = schema().getFields().get(unmapIndex(i));
            Class<?> clazz = fld.getType();
            
            if (clazz.equals(java.sql.Date.class)) {
                cal.clear(Calendar.HOUR_OF_DAY);
                cal.clear(Calendar.MINUTE);
                cal.clear(Calendar.SECOND);
                return new java.sql.Date(cal.getTimeInMillis());
            } else if (clazz.equals(java.sql.Time.class)) {
                cal.clear(Calendar.YEAR);
                cal.clear(Calendar.MONTH);
                cal.clear(Calendar.DAY_OF_MONTH);
            } else if (clazz.equals(java.sql.Timestamp.class)) {
                return new java.sql.Time(cal.getTimeInMillis());
            } {
                return cal.getTime();
            }
        }

        @Override
        protected void put(String key, Object value) {
            throw new UnsupportedOperationException();
        }
    
        @Override
        protected void set(int index, Object value) {
            throw new UnsupportedOperationException();
        }
    
        @Override
        protected List<Object> list() {
            Schema schema = schema();
            List<Object> list = new ArrayList<Object>(schema.getFields().size());

            for (int i = 0; i < schema.size(); i++) {
                list.add(get(i));
            }
            return list;
        }
    
        @Override
        protected Map<String, Object> map() {
            Map<String,Object> map = new LinkedHashMap<String, Object>();

            for (Field fld : schema()) {
                map.put(fld.getName(), get(fld.getName()));
            }
            return map;
        }
    }
}
