package org.jeo.cli.conv;

import org.jeo.filter.Filter;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.IStringConverterFactory;
import com.vividsolutions.jts.geom.Envelope;

public class JeoCLIConverterFactory implements IStringConverterFactory {

    @Override
    public Class<? extends IStringConverter<?>> getConverter(Class clazz) {
        if (clazz == Envelope.class) {
            return EnvelopeConverter.class;
        }
        if (clazz == Filter.class) {
            return FilterConverter.class;
        }
        if (clazz == CoordinateReferenceSystem.class) {
            return CRSConverter.class;
        }
        return null;
    }

}
