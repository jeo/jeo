package org.jeo.cli.conv;

import com.beust.jcommander.IStringConverter;
import com.vividsolutions.jts.geom.Envelope;

public class EnvelopeConverter implements IStringConverter<Envelope> {

    @Override
    public Envelope convert(String str) {
        String[] split = str.split(",");
        if (split.length != 4) {
            throw new IllegalArgumentException("bbox syntax: <xmin,ymin,xmax,ymax>");
        }

        return new Envelope(Double.parseDouble(split[0]), Double.parseDouble(split[2]), 
            Double.parseDouble(split[1]), Double.parseDouble(split[3]));
    }

}
