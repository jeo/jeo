package org.jeo.geopkg;

import org.jeo.feature.Schema;
import org.jeo.geom.Geom;
import org.jeo.sql.PrimaryKey;

/**
 * Feature entry in a geopackage.
 * <p>
 * This class corresponds to the "geometry_columns" table.
 * </p>
 * @author Justin Deoliveira, OpenGeo
 */
public class FeatureEntry extends Entry {

    Geom.Type geometryType;
    Integer coordDimension;
    String geometryColumn;

    Schema schema;
    PrimaryKey primaryKey;

    public FeatureEntry() {
        setDataType(DataType.Feature);
    }

    public String getGeometryColumn() {
        return geometryColumn;
    }

    public void setGeometryColumn(String geometryColumn) {
        this.geometryColumn = geometryColumn;
    }

    public Geom.Type getGeometryType() {
        return geometryType;
    }

    public void setGeometryType(Geom.Type geometryType) {
        this.geometryType = geometryType;
    }

    public Integer getCoordDimension() {
        return coordDimension;
    }

    public void setCoordDimension(Integer coordDimension) {
        this.coordDimension = coordDimension;
    }

    Schema getSchema() {
        return schema;
    }

    void setSchema(Schema schema) {
        this.schema = schema;
    }

    PrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    void setPrimaryKey(PrimaryKey primaryKey) {
        this.primaryKey = primaryKey;
    }

    void init(FeatureEntry e) {
        super.init(e);
        setGeometryColumn(e.getGeometryColumn());
        setGeometryType(e.getGeometryType());
        setCoordDimension(e.getCoordDimension());
    }

    @Override
    FeatureEntry copy() {
        FeatureEntry e = new FeatureEntry();
        e.init(this);
        return e;
    }
}
