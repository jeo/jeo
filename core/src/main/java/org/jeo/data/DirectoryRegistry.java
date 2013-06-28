package org.jeo.data;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import org.jeo.data.mem.MemWorkspace;
import org.jeo.util.Util;

/**
 * A registry that loads workspaces from files in a specified directory.
 *  
 * @author Justin Deoliveira, OpenGeo
 */
public class DirectoryRegistry implements Registry {

    /** base directory */
    File baseDir;

    /** list of file extensions to restrict to */
    List<String> exts;

    /**
     * Constructs a new registry.
     * 
     * @param baseDir The directory to search for files in.
     * @param exts Optional file name extensions to restrict look ups to.
     */
    public DirectoryRegistry(File baseDir, String... exts) {
        this.baseDir = baseDir;
        this.exts = exts.length > 0 ? Arrays.asList(exts) : null; 
    }

    @Override
    public Iterable<String> list() {
        LinkedHashSet<String> set = new LinkedHashSet<String>();

        if (exts == null) {
            for (String fn : baseDir.list()) {
                set.add(Util.base(fn));
            }
        }
        else {
            for (final String ext : exts) {
                String[] files = baseDir.list(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        int dot = name.lastIndexOf('.');
                        return ext.equalsIgnoreCase(name.substring(dot+1));
                    }
                });
                for (String file : files) {
                    set.add(Util.base(file));
                }
            }
        }

        return set;
    }

    @Override
    public Workspace get(final String key) throws IOException {
        if (exts == null) {
            //search for any file with this base name
            String[] files = baseDir.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.startsWith(key+".");
                }
            });
            for (String file : files) {
                Workspace ws = workspace(new File(baseDir, file));
                if (ws != null) {
                    return ws;
                }
            }
        }
        else {
            //look for one of our specific extensions
            for (String ext : exts) {
                File f = new File(baseDir, key + "." + ext);
                if (f.exists()) {
                    Workspace ws = workspace(f);
                    if (ws != null) {
                        return ws;
                    }
                }
            }
        }

        return null;
    }

    Workspace workspace(File file) throws IOException {
        Object obj = Drivers.open(file);
        if (obj instanceof Workspace) {
            return (Workspace) obj;
        }
        else if (obj instanceof Dataset) {
            Dataset data = (Dataset) obj;
            MemWorkspace mem = new MemWorkspace();
            mem.put(data.getName(), data);
            return mem;
        }
        else {
            return null;
        }
    }

    public void close() {
    }
}
