package org.jeo.ogr;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

public class SpatiaLite extends OGRDriver<OGRWorkspace> {

    public static OGRWorkspace open(File file) throws IOException {
        return new SpatiaLite().open((Map)Collections.singletonMap(FILE, file));
    }

    @Override
    public String getName() {
        return "SpatiaLite";
    }
    
    @Override
    public Class<OGRWorkspace> getType() {
        return OGRWorkspace.class;
    }
    
    @Override
    protected OGRWorkspace open(OGRWorkspace workspace) throws IOException {
        return workspace;
    }
    
    @Override
    protected String getOGRDriverName() {
        return "SQLite";
    }

}
