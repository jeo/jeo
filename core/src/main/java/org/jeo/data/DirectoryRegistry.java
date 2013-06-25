package org.jeo.data;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

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
    public Iterator<String> keys() {
        LinkedHashSet<String> set = new LinkedHashSet<String>();

        if (exts == null) {
            for (String fn : baseDir.list()) {
                set.add(basename(fn));
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
                    set.add(basename(file));
                }
            }
        }

        return set.iterator();
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
                Workspace ws = Drivers.open(new File(baseDir, file), Workspace.class);
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
                    Workspace ws = Drivers.open(f, Workspace.class);
                    if (ws != null) {
                        return ws;
                    }
                }
            }
        }

        return null;
    }

    public void close() {
    }

    String basename(String fn) {
        int dot = fn.lastIndexOf('.');
        return fn.substring(0, dot);
    }
}
