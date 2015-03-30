/* Copyright 2013 The jeo project. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jeo.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.jeo.json.JSONObject;
import org.jeo.json.JSONValue;
import org.jeo.map.Style;
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
        //final Map<String,String> metaFiles = new HashMap<String, String>();
        CompositeFilenameFilter fileFilter = new CompositeFilenameFilter();
        if (exts != null) {
            fileFilter.and(new ExtensionFilter(exts));
        }

        Map<String,FileGroup> files = listFiles(fileFilter);

        // process files to see what ones we have drivers for
        LinkedHashSet<Handle<?>> items = new LinkedHashSet<Handle<?>>();
        for (Map.Entry<String, FileGroup> e : files.entrySet()) {
            String name = e.getKey();
            FileGroup grp = e.getValue();

            Driver<?> drv = null;

            // check for a meta file
            if (grp.hasMeta()) {
                Pair<Driver<?>,Map<String,Object>> meta = readMetaFile(grp);
                if (meta != null) {
                    drv = meta.first;
                }
            }

            if (drv == null) {
                // no meta file, look through list and try to load one of them
                Iterator<File> it = grp.files().iterator();
                while(drv == null && it.hasNext()) {
                    drv = Drivers.find(it.next().toURI(), drivers);
                }
            }

            if (drv != null) {
                Handle<?> h = RepoHandle.to(name, handleType(drv), drv, this);
                if (filter.test(h)) {
                    items.add(h);
                }
            }
            else {
                LOG.debug("unable to load driver for files: " + grp.files());
            }
        }

        return items;
    }

    @Override
    public <T> T get(final String key, Class<T> type) throws IOException {
        CompositeFilenameFilter fileFilter = new CompositeFilenameFilter(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                String base = Util.base(name);
                return base.equalsIgnoreCase(key);
            }
        });
        if (exts != null) {
            fileFilter.and(new ExtensionFilter(exts));
        }

        Map<String,FileGroup> files = listFiles(fileFilter);
        if (!files.isEmpty()) {
            FileGroup grp = files.values().iterator().next();
            return objOrNull(grp, type);
        }

        return null;
    }

    @Override
    public void close() {
    }

    Pair<Driver<?>,Map<String,Object>> readMetaFile(FileGroup grp) {
        File f = grp.meta();
        try {
            BufferedReader r = Files.newBufferedReader(f.toPath(), Charset.forName("UTF-8"));
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

            // process the options, and turn any relative file urls to absolute ones relative to 
            // the meta file
            for (Map.Entry<String, Object> e : opts.entrySet()) {
               if (FileDriver.FILE.name().equals(e.getKey())) {
                   File file = FileDriver.FILE.get(opts);
                   if (file != null && !file.isAbsolute()) {
                       file = new File(f.getParentFile(), file.getPath());
                       e.setValue(file);
                   }
               }
            }

            if (drv instanceof FileDriver && !FileDriver.FILE.in(opts) && !grp.files().isEmpty()) {
                // TODO: instead of grab first perhaps we should figure out what the primary 
                // file is
                opts.put(FileDriver.FILE.name(), grp.files().get(0));
            }

            return new Pair(drv, opts);
        }
        catch(Exception e) {
            LOG.debug("Error parsing meta file: " + f.getPath(), e);
            return null;
        } 
    }

    <T> T objOrNull(FileGroup grp, Class<T> type) throws IOException {
        if (grp.hasMeta()) {
            Pair<Driver<?>,Map<String,Object>> meta = readMetaFile(grp);
            if (meta != null) {
                Object data = meta.first.open(meta.second);
                return objOrNull(data, grp, type);
            }
        }
        else {
            // regular direct file base
            Iterator<File> it = grp.files().iterator();
            Object obj = null;
            while (obj == null && it.hasNext()) {
                obj = Drivers.open(it.next(), type, drivers);
            }

            return objOrNull(obj, grp, type);
        }

        return null;
    }

    <T> T objOrNull(Object obj, FileGroup grp, Class<T> type) throws IOException {
        if (obj != null) {
            if (obj instanceof Dataset) {
                obj = new SingleWorkspace((Dataset) obj);
            }
        }
        else {
            LOG.debug("Unable to open file: " + grp.files());
        }
        
        return type.cast(obj);
    }

    Class<?> handleType(Driver<?> drv) {
        return Style.class.isAssignableFrom(drv.type()) ? Style.class : Workspace.class;
    }

    Map<String,FileGroup> listFiles(FilenameFilter filter) {
        LinkedHashMap<String, FileGroup> map = new LinkedHashMap<String, FileGroup>();
        for (String file : baseDir.list(filter)) {
            String base = Util.base(file);
            FileGroup grp = map.get(base);
            if (grp == null) {
                grp = new FileGroup();
                map.put(base, grp);
            }

            grp.add(new File(baseDir, file));
        }
        return map;
    }

    static class FileGroup {

        private File meta = null;
        private List<File> files = new ArrayList<File>();

        void add(File file) {
            if ("jeo".equalsIgnoreCase(Util.extension(file.getName()))) {
                meta = file;
            }
            else {
                files.add(file);
            }
        }

        List<File> files() {
            return files;
        }

        boolean hasMeta() {
            return meta != null;
        }

        File meta() {
            return meta;
        }

        @Override
        public String toString() {
            return files.toString();
        }
    }

    static class CompositeFilenameFilter implements FilenameFilter {

        List<FilenameFilter> filters = new ArrayList<FilenameFilter>();

        public CompositeFilenameFilter(FilenameFilter... filters) {
            this.filters.addAll(Arrays.asList(filters));
        }

        public CompositeFilenameFilter and(FilenameFilter filter) {
            filters.add(filter);
            return this;
        }

        @Override
        public boolean accept(File dir, String name) {
            boolean accept = true;
            Iterator<FilenameFilter> it = filters.iterator();
            while (accept && it.hasNext()) {
                accept = it.next().accept(dir, name);
            }
            return accept;
        }

    }

    static class ExtensionFilter implements FilenameFilter {

        List<String> exts;

        ExtensionFilter(List<String> exts) {
            this.exts = exts;
        }

        @Override
        public boolean accept(File dir, String name) {
            String ext = Util.extension(name);
            return exts.contains(ext);
        }

    }
 }
