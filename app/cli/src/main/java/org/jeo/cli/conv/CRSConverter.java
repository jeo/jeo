package org.jeo.cli.conv;

import org.jeo.proj.Proj;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.beust.jcommander.IStringConverter;

public class CRSConverter implements IStringConverter<CoordinateReferenceSystem> {

    @Override
    public CoordinateReferenceSystem convert(String str) {
        return Proj.crs(str);
    }

}
