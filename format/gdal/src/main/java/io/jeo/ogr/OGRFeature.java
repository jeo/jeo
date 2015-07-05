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

import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.gdal.ogr.Feature;
import io.jeo.vector.Field;
import io.jeo.vector.Schema;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;

public class OGRFeature implements io.jeo.vector.Feature {

    Feature f;
    Schema schema;

    public OGRFeature(Feature f, Schema schema) {
        this.f = f;
        this.schema = schema;
    }

    @Override
    public String id() {
        return String.valueOf(f.GetFID());
    }

    @Override
    public boolean has(String key) {
        return "geometry".equals(key) || f.GetFieldIndex(key) >= 0;
    }

    @Override
    public Object get(String key) {
        return "geometry".equals(key) ? geometry() : field(f.GetFieldIndex(key));
    }

    @Override
    public Geometry geometry() {
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
    public io.jeo.vector.Feature put(String key, Object val) {
        throw new UnsupportedOperationException();
    }

    @Override
    public io.jeo.vector.Feature put(Geometry g) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Object> map() {
        Map<String,Object> map = new LinkedHashMap<>();

        for (Field fld : schema) {
            map.put(fld.name(), get(fld.name()));
        }
        return map;
    }

    protected Object field(int i) {
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

        Calendar cal = Calendar.getInstance(TimeZone.getDefault(), Locale.ROOT);

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

        Field fld = schema.fields().get(unmapIndex(i));
        Class<?> clazz = fld.type();

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

    int mapIndex(int i) {
        return schema.geometry() == null ? i : i - 1;
    }

    int unmapIndex(int i) {
        return schema.geometry() == null ? i : i + 1;
    }
}
