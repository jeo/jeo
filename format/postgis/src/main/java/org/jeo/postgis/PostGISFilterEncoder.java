package org.jeo.postgis;

import org.jeo.feature.Field;
import org.jeo.sql.FilterSQLEncoder;

import com.vividsolutions.jts.geom.Geometry;

public class PostGISFilterEncoder extends FilterSQLEncoder {

    PostGISDataset dataset;

    public PostGISFilterEncoder(PostGISDataset dataset) {
        setPrimaryKey(dataset.getTable().getPrimaryKey());
        setDbTypes(dataset.pg.getDbTypes());
        setSchema(dataset.getSchema());
    }

    @Override
    protected int srid(Geometry geo, Object obj) {
        if (geo.getSRID() == 0 && obj instanceof Field) {
            Field fld = (Field) obj;
            Integer srid = fld.property("srid", Integer.class);
            if (srid != null) {
                return srid;
            }
        }
        return super.srid(geo, obj);
    }
}
