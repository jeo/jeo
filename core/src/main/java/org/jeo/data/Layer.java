package org.jeo.data;

import java.io.IOException;

import com.vividsolutions.jts.geom.Envelope;

public interface Layer {

    Envelope bounds() throws IOException;
}
