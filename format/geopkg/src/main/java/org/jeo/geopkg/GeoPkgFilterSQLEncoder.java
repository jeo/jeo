package org.jeo.geopkg;

import org.jeo.filter.Spatial;
import org.jeo.sql.FilterSQLEncoder;

public class GeoPkgFilterSQLEncoder extends FilterSQLEncoder {

    @Override
    public Object visit(Spatial spatial, Object obj) {
        abort(spatial, "Spatial filters unsupported");
        return null;
    }
}
