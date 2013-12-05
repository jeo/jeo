package org.jeo.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.jeo.json.JSONObject;
import org.jeo.json.JSONValue;
import org.jeo.util.Pair;
import org.jeo.filter.Filter;
import org.jeo.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A repository that loads workspaces from files in a specified directory.
 *  
 * @author Justin Deoliveira, OpenGeo
 */
public class DirectoryRepository implements DataRepository {

    static Logger LOG = LoggerFactory.getLogger(DirectoryRepository.class);

    /** base directory */
    File baseDir;

    /** driver registry */
    DriverRegistry drivers;

    /** list of file extensions to restrict to */
    List<String> exts;

    /**
     * Constructs a new registry.
     * 
     * @param baseDir The directory to search for files in.
     * @param exts Optional file name extensions to restrict look ups to.
     */
    public DirectoryRepository(File baseDir, String... exts) {
         this(baseDir, Drivers.REGISTRY, exts);
    }

    /**
     * Constructs a new registry.
     * 
     * @param baseDir The directory to search for files in.
     * @param exts Optional file name extensions to restrict look ups to.
     */
    public DirectoryRepository(File baseDir, DriverRegistry drivers, String... exts) {
        if (baseDir == null) {
            throw new NullPointerException("baseDir must not be null");
        }
        if (!baseDir.exists()) {
            throw (IllegalArgumentException) new IllegalArgumentException()
                .initCause(new FileNotFoundException(baseDir.getPath()));
        }

        this.baseDir = baseDir;
        this.drivers = drivers;
        this.exts = exts.length > 0 ? Arrays.asList(exts) : null; 
    }

    /**
     * The base directory of the repository.
     */
    public File getDirectory() {
        return baseDir;
    }

    @Override
    public Iterable<Handle<?>> query(Filter<? super Handle<?>> filter)
        throws IOException {

        // list all files, possibly filtering by extension
        final Map<String,String> metaFiles = new HashMap<String, String>();

        FilenameFilter fileFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                String ext = Util.extension(name);
                if ("jeo".equalsIgnoreCase(ext)) {
                    // meta file, save for later
                    metaFiles.put(Util.base(name), name);
                    return false;
                }

                return exts != null ? exts.contains(ext) : true;
            }
        };

        String[] files = baseDir.list(fileFilter);

        // process files to see what ones we have drivers for
        LinkedHashSet<Handle<?>> items = new LinkedHashSet<Handle<?>>();
        for (String fn : files) {
            File file = new File(baseDir, fn);
            String name = Util.base(fn);

            Driver<?> drv = null;

            // check for a meta file
            if (metaFiles.containsKey(name)) {
                File metaFile = new File(baseDir, metaFiles.get(name));
                Pair<Driver<?>,Map<String,Object>> meta = readMetaFile(metaFile);
                if (meta != null) {
                    drv = meta.first();
                }

                // remove the meta file from the list
                metaFiles.remove(name);
            }

            if (drv == null) {
                drv = Drivers.find(file.toURI(), drivers);
            }

            if (drv != null) {
                Handle<?> h = Handle.to(name, drv, this);
                if (filter.apply(h)) {
                    items.add(h);
                }
            }
            else {
                LOG.debug("unable to load driver for file: " + file.getPath());
            }
        }

        // handle left over meta files
        for (Map.Entry<String, String> e : metaFiles.entrySet()) {
            String name = e.getKey();
            File file = new File(baseDir, e.getValue());

            Pair<Driver<?>,Map<String,Object>> meta = readMetaFile(file);
            if (meta != null) {
                Handle<?> h = Handle.to(name, meta.first(), this);
                if (filter.apply(h)) {
                    items.add(h);
                }
            }
        }
        return items;
    }

    @Override
    public <T> T get(final String key, Class<T> type) throws IOException {
        if (exts == null) {
            //search for any file with this base name
            String[] files = baseDir.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.startsWith(key+".");
                }
            });
            for (String file : files) {
                return objOrNull(new File(baseDir, file), type);
            }
        }
        else {
            //look for one of our specific extensions
            for (String ext : exts) {
                File f = new File(baseDir, key + "." + ext);
                if (f.exists()) {
                    return objOrNull(f, type);
                }
            }
        }

        return null;
    }

    @Override
    public void close() {
    }

    Pair<Driver<?>,Map<String,Object>> readMetaFile(File f) {
        try {
            BufferedReader r = new BufferedReader(new FileReader(f));
            JSONObject obj = (JSONObject) JSONValue.parseWithException(r);

            // get the driver
            String drvName = obj.containsKey("driver") ? obj.get("driver").toString() : null;
            if (drvName == null) {
                LOG.debug("meta file " + f.getPath() + " specified no driver");
                return null;
            }

            Driver<?> drv = Drivers.find(drvName, drivers);
            if (drv == null) {
                LOG.debug("meta file " + f.getPath() + " specified unknown driver: " + drvName);
                return null;
            }

            // read the connection options
            Map<String,Object> opts = new LinkedHashMap();
            if (obj.containsKey("options")) {
                Object optsObj = obj.get("options");
                if (optsObj instanceof Map) {
                    opts = (Map) optsObj;
                }
                else {
                    LOG.debug("meta file " + f.getPath() + " 'options' key is not an object");
                    return null;
                }
            }

            return new Pair(drv, opts);
        }
        catch(Exception e) {
            LOG.debug("Error parsing meta file: " + f.getPath(), e);
            return null;
        } 
    }

    <T> T objOrNull(File file, Class<T> type) throws IOException {
        // handle a meta file
        String base = Util.base(file.getName());
        String ext = Util.extension(file.getName());

        if ("jeo".equalsIgnoreCase(ext)) {
            Pair<Driver<?>,Map<String,Object>> meta = readMetaFile(file);
            if (meta != null) {
                Object data = meta.first().open(meta.second());
                return objOrNull(data, file, type);
            }
        }
        else {
            //check for a sidecar meta file
            File metaFile = new File(baseDir, base + ".jeo");
            if (metaFile.exists()) {
                Pair<Driver<?>,Map<String,Object>> meta = readMetaFile(metaFile);
                if (meta != null) {
                    Map<String,Object> opts = meta.second();
                    opts.put(FileDriver.FILE.getName(), file);

                    return objOrNull(meta.first().open(opts), file, type);
                }
            }
            else {
                // regular direct file base
                Object obj = Drivers.open(file, type, drivers);
                return objOrNull(obj, file, type);
            }
        }
        return null;
    }

    <T> T objOrNull(Object obj, File file, Class<T> type) throws IOException {
        if (obj != null) {
            if (obj instanceof Dataset) {
                obj = new SingleWorkspace((Dataset) obj);
            }
        }
        else {
            LOG.debug("Unable to open file: " + file.getPath());
        }
        
        return type.cast(obj);
    }

}
