package org.jeo.data;

import java.io.IOException;
import java.util.Map;

import org.jeo.feature.Schema;
import org.jeo.util.Key;

/**
 * A container of {@link Dataset} objects.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public interface Workspace extends Disposable {

    /**
     * The driver used to open the workspace.
     */
    Driver<?> getDriver();

    /**
     * The driver options for the workspace.
     */
    Map<Key<?>,Object> getDriverOptions();

    /**
     * The names of all datasets of the workspace.
     * 
     * @return Iterable over datasets. 
     */
    Iterable<Handle<Dataset>> list() throws IOException;

    /**
     * Returns a layer object by name.
     * 
     * @param layer Name of the layer.
     * 
     * @return The Layer object, or <code>null</code> if no such layer exists.
     */
    Dataset get(String layer) throws IOException;

    /**
     * Creates a new vector layer in the workspace.
     * <p>
     * This method should throw {@link UnsupportedOperationException} if the workspace is not 
     * capable of creating new vector layers.
     * </p>
     * @param schema The schema of the vector layer.
     * 
     */
    VectorDataset create(Schema schema) throws IOException;

    /**
     * Closes the workspace.
     * <p>
     * Application code should always call this method when the workspace is no longer needed. 
     * </p>
     */
    void close();
}
