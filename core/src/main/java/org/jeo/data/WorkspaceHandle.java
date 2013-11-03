package org.jeo.data;

import java.io.IOException;

/**
 * Handle for a {@link Workspace} object.
 * 
 * @author Justin Deoliveira, Boundless
 */
public class WorkspaceHandle extends Handle<Workspace> {

    DataRepository repo;

    public WorkspaceHandle(String name, Driver<?> driver, DataRepository parent) {
        super(name, Workspace.class, driver);
        this.repo = parent;
    }

    @Override
    protected Workspace doResolve() throws IOException {
        return repo.get(name);
    }

}
