package org.jeo.data;

import java.io.IOException;
import java.util.Map;

/**
 * Creates a {@link Workspace} object from a set of named parameters.
 * <p>
 * A {@link Workspace} implementation should register an instance of this interface with 
 * {@link Workspaces#registerWorkspaceFactory(WorkspaceFactory)}.
 * </p>
 * @author Justin Deoliveira, OpenGeo
 *
 * @param <T> The workspace class.
 * @see Workspaces#registerWorkspaceFactory(WorkspaceFactory)
 */
public interface WorkspaceFactory<T extends Workspace> {

    /**
     * Creates a new workspace from a set of well known named parameters.
     * <p>
     * This method must return <code>null</code> if any required parameters are not present in
     * the map.  
     * </p>
     * @param map The parameter map.
     */
    T create(Map<String,Object> map) throws IOException;
}
