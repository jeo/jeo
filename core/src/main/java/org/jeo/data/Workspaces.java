package org.jeo.data;

import java.io.File;

import org.jeo.data.mem.MemWorkspace;

/**
 * Utility class for working with {@link Workspace} objects.
 * <p>
 * 
 * </p>
 * @author Justin Deoliveira, Boundless
 */
public class Workspaces {

    public static Workspace fromMemory() {
        //return new MemWorkspace();
        return null;
    }

    public static Workspace fromDirectory(File file) {
        return null;
    }

    public static Workspace fromJSON(File file) {
        return null;
    }

    public static Workspace fromDataset(Dataset dataset) {
        return null;
    }
}
