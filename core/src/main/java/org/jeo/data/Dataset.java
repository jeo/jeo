package org.jeo.data;

import java.io.IOException;

import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

/**
 * A geospatial dataset.
 * 
 * @author Justin Deoliveira, OpenGeo
 *
 * @see VectorData
 * @see TileSet
 */
public interface Dataset extends Disposable {

    /**
     * The driver used to open the dataset.
     */
    Driver<?> getDriver();

    /**
     * Name of the layer.
     * <p>
     * The value used to look up a layer in a workspace.
     * </p>
     */
    String getName();

    /**
     * Human readable title of the layer, or <code>null</code>. 
     */
    String getTitle();

    /**
     * Human readable description of the layer, or <code>null</code>. 
     */
    String getDescription();

    /**
     * The native coordinate reference system of the layer, or <code>null</code> if unknown. 
     */
    CoordinateReferenceSystem getCRS();

    /**
     * The extent of the dataset in its native coordinate reference system.
     * <p>
     * In the event the layer is empty this method should return an null envelope with 
     * {@link Envelope#setToNull()}.
     * </p>
     */
    Envelope bounds() throws IOException;
}
