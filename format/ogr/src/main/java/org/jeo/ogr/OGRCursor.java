package org.jeo.ogr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.gdal.ogr.Layer;
import org.jeo.data.Cursor;
import org.jeo.feature.Feature;
import org.jeo.feature.Field;
import org.jeo.feature.ListFeature;

import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;

public class OGRCursor extends Cursor<Feature> {

    Layer layer;
    OGRDataset dataset;
    org.gdal.ogr.Feature next;

    public OGRCursor(Layer layer, OGRDataset dataset) {
        this.layer = layer;
        this.dataset = dataset;
    }

    @Override
    public boolean hasNext() throws IOException {
        if (next == null) {
            next = layer.GetNextFeature();
        }
        return next != null;
    }

    @Override
    public Feature next() throws IOException {
        List<Object> values = new ArrayList<Object>();
        for (Field f : dataset.getSchema()) {
            if (f.isGeometry()) {
                byte[] wkb = next.GetGeometryRef().ExportToWkb();
                try {
                    values.add(new WKBReader().read(wkb));
                } catch (ParseException e) {
                    throw new IOException(e);
                }
            }
            else {
                Class<?> t = f.getType();
                if (t == Integer.class) {
                    values.add(next.GetFieldAsInteger(f.getName()));
                }
                else if (t == Double.class) {
                    values.add(next.GetFieldAsDouble(f.getName()));
                }
                else if (t == Date.class) {
                    int i = next.GetDefnRef().GetFieldIndex(f.getName());
                    int[] year = new int[1];
                    int[] mon = new int[1];
                    int[] day = new int[1];
                    int[] hour = new int[1];
                    int[] min = new int[1];
                    int[] sec = new int[1];
                    int[] tzone = new int[1];

                    next.GetFieldAsDateTime(i, year, mon, day, hour, min, sec, tzone);

                    Calendar cal = Calendar.getInstance();
                    // from ogr_core.h 
                    // 0=unknown, 1=localtime(ambiguous), 100=GMT, 104=GMT+1, 80=GMT-5, etc
                    int tz = tzone[0];
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
                    cal.set(Calendar.MONTH, mon[0]);
                    cal.set(Calendar.DAY_OF_MONTH, day[0]);
                    cal.set(Calendar.HOUR_OF_DAY, hour[0]);
                    cal.set(Calendar.MINUTE, min[0]);
                    cal.set(Calendar.SECOND, sec[0]);
                    values.add(cal.getTime());
                }
                else {
                    values.add(next.GetFieldAsString(f.getName()));
                }
            }
        }

        String fid = String.valueOf(next.GetFID());
        next = null;
        return new ListFeature(fid, values, dataset.getSchema());
    }

    @Override
    public void close() throws IOException {
    }


}
