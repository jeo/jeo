package org.jeo.data;

import java.io.IOException;

/**
 * A repository of workspace objects. 
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public interface DataRepository extends Disposable {

    /**
     * Collection of handles for workspaces in the repository.
     */
    Iterable<WorkspaceHandle> list() throws IOException;

    /**
     * Returns a workspace object by name.
     * <p>
     * If the repository supports a "default" workspace in the repository it should return it from
     * this method when the empty string is passed as the <tt>name</tt>.
     * </p>
     * @param name The name of the workspace.
     * 
     * @return The workspace or <code>null</code> if so such object matching the name exists.
     */
    Workspace get(String name) throws IOException;

}
