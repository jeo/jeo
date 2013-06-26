package org.jeo.geopkg;

import java.util.Map;

import org.jeo.sql.DbTypes;

public class GeoPkgTypes extends DbTypes {

    @Override
    protected Map<String, Class<?>> createNameMappings() {
        Map<String,Class<?>> map = super.createNameMappings();

        map.put("INTEGER", Integer.class);
        map.put("REAL", Double.class);
        map.put("TEXT", String.class);
        map.put("BLOB", byte[].class);

        return map;
    }
}
