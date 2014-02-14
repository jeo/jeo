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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jeo.util.Key;
import org.jeo.util.Messages;
import org.jeo.util.Util;

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
    public boolean canOpen(Map<?, Object> opts, Messages msgs) {
        if (!FILE.has(opts)) {
            Messages.of(msgs).report("No " + FILE + " option specified");
            return false;
        }

        File file = file(opts);
        if (file == null) {
            Messages.of(msgs).report("Unable to obtain file from " + FILE.raw(opts));
            return false;
        }

        if (!file.canRead()) {
            Messages.of(msgs).report("Unable to read file " + file.getPath());
            return false;
        }

        return canOpen(file, opts, msgs);
    }

    /**
     * Same semantics as {@link Driver#canOpen(Map)}, and supplies the file to open. 
     * 
     * @param file The file to open.
     * @param opts The original driver options.
     * @param msgs Messages to report back.
     *
     */
    protected boolean canOpen(File file, Map<?, Object> opts, Messages msgs) {
        return true;
    }

    /**
     * Parses the {@link FileDriver#FILE} key and calls through to {@link #open(File, Map)}. 
     */
    @Override
    public final T open(Map<?, Object> opts) throws IOException {
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
    protected abstract T open(File file, Map<?,Object> opts) throws IOException;

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
        return Util.extension(f.getName());
    }
}
