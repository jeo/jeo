package org.jeo.geopkg;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Entry in a geopackage.
 * <p>
 * This class corresponds to the "geopackage_contents" table.
 * </p>
 * @author Justin Deoliveira, OpenGeo
 *
 */
public class Entry {

    public static enum DataType {
        Feature("features"), Raster("rasters"), Tile("tiles"), 
        FeatureWithRaster("featuresWithRasters");

        String value;
        DataType(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }
    }

    static SimpleDateFormat dateFormat() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-mm-dd'T'HH:MM:ss.SSS'Z'");
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        return format;
    }

    protected String tableName;
    protected DataType dataType;
    protected String identifier;
    protected String description;
    protected String lastChange;
    protected Envelope bounds;
    protected Integer srid;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLastChange() {
        return lastChange;
    }

    public Date lastChange() {
        try {
            return lastChange != null ? dateFormat().parse(lastChange) : null;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public void setLastChange(String lastChange) {
        this.lastChange = lastChange;
    }

    public void lastChange(Date date) {
        lastChange = dateFormat().format(date);
    }

    public Envelope getBounds() {
        return bounds;
    }

    public void setBounds(Envelope bounds) {
        this.bounds = bounds;
    }

    public Integer getSrid() {
        return srid;
    }

    public void setSrid(Integer srid) {
        this.srid = srid;
    }

    void init(Entry e) {
        setDescription(e.getDescription());
        setIdentifier(e.getIdentifier());
        setDataType(e.getDataType());
        setBounds(e.getBounds());
        setSrid(e.getSrid());
        setTableName(e.getTableName());
    }

    Entry copy() {
        Entry e = new Entry();
        e.init(this);
        return e;
    }
}
