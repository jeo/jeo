package org.jeo.data;

import java.io.IOException;

/**
 * A container of {@link Workspace} objects.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public interface Registry extends Disposable {

    /**
     * The names of all workspaces of the registry.
     */
    Iterable<String> list();

    /**
     * Returns a workspace object by name.
     * 
     * @param key The name/key of the workspaces.
     * 
     * @return The workspace or <code>null</code> if so such workspaces matching the key exists.
     */
    Workspace get(String key) throws IOException;

}
