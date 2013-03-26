package org.jeo.data;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.jeo.util.Util;

/**
 * Extension of {@link WorkspaceFactory} for workspaces that are filed based. 
 * <p>
 * Subclasses typically only need to implement {@link #createFromFile(File)} and specify the 
 * supported file extensions in its constructor.
 * </p>
 * @author Justin Deoliveira, OpenGeo
 */
public abstract class FileWorkspaceFactory<T extends Workspace> implements WorkspaceFactory<T> {

    /**
     * Parameter specifying the file parameter for the workspace.
     */
    public static final String FILE = "file";

    /**
     * List of supported file extensions.
     */
    List<String> exts;

    /**
     * Constructs the factory with the set of supported extensions. 
     */
    protected FileWorkspaceFactory(String... exts) {
        this.exts = Arrays.asList(exts);
    }

    @Override
    public T create(Map<String, Object> map) throws IOException {
        if (map.containsKey(FILE)) {
            File file = toFile(map.get(FILE));
            if (file != null) {
                String fn = file.getName();
                int dot = fn.lastIndexOf('.');
                if (exts.contains(fn.substring(dot+1))) {
                    return createFromFile(file);    
                }
            }
        }
        return null;
    }

    /**
     * Converts the object passed via the parameter map with the key {@link #FILE} to a 
     * {@link File}.
     * <p>
     *  Subclasses should extend/override this method to provide additional conversion method.
     * </p>
     *  
     * @param object The file object. 
     * @return The object converted to {@link File}.
     */
    protected File toFile(Object object) {
        return Util.toFile(object);
    }

    /**
     * Creates the workspace from a file object.
     * 
     */
    protected abstract T createFromFile(File file) throws IOException;
}
