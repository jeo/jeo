package org.jeo.protobuf;

import org.osgeo.proj4j.CoordinateReferenceSystem;

public class ProtobufHeader {

    CoordinateReferenceSystem crs;

    public CoordinateReferenceSystem getCRS() {
        return crs;
    }

    public void setCRS(CoordinateReferenceSystem crs) {
        this.crs = crs;
    }

}
