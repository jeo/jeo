package org.jeo.data;

import java.io.File;

/**
 * Interface implemented by {@link Dataset} and {@link Workspace} objects that are file based.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public interface FileData {

    /**
     * Gets the backing file.
     */
    File getFile();
}
