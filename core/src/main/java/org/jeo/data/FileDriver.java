package org.jeo.data;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jeo.util.Key;

/**
 * Base class for file based drivers.
 *  
 * @author Justin Deoliveira, OpenGeo
 *
 * @param <T>
 */
public abstract class FileDriver<T> implements Driver<T> {

    /**
     * Key specifying the file path.
     */
    public static final Key<File> FILE = new Key<File>("file", File.class);

    @Override
    public List<String> getAliases() {
        return Collections.emptyList();
    }

    @Override
    public List<Key<?>> getKeys() {
        return (List) Arrays.asList(FILE);
    }

    /**
     * Checks for the existence of the {@link FileDriver#FILE} key and calls through to 
     * {@link #canOpen(File, Map)} 
     */
    @Override
    public boolean canOpen(Map<?, Object> opts) {
        if (!FILE.has(opts)) {
            return false;
        }

        File file = file(opts);
        if (file == null) {
            return false;
        }

        return canOpen(file, opts);
    }

    /**
     * Same semantics as {@link Driver#canOpen(Map)}, and supplies the file to open. 
     * 
     * @param file The file to open.
     * @param opts The original driver options.
     *
     */
    public boolean canOpen(File file, Map<?, Object> opts) {
        return file.canRead();
    }

    /**
     * Parses the {@link FileDriver#FILE} key and calls through to {@link #open(File, Map)}. 
     */
    @Override
    public T open(Map<?, Object> opts) throws IOException {
        File file = file(opts);
        return file != null ? open(file, opts) : null;
    }

    /**
     * Same semantics as {@link Driver#open(Map)}, and supplies the file to open. 
     * 
     * @param file The file to open.
     * @param opts The original driver options.
     *
     */
    public abstract T open(File file, Map<?,Object> opts) throws IOException;

    /**
     * Helper to pull file object out of option map.
     */
    protected File file(Map<?,Object> opts) {
        return FILE.get(opts);
    }

    /**
     * Helper to get file extension (lower case). 
     */
    protected String ext(File f) {
        int dot = f.getName().lastIndexOf('.');
        return dot != -1 ? f.getName().substring(dot+1).toLowerCase() : null;
    }
}
