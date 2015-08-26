/* Copyright 2015 The jeo project. All rights reserved.
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
package io.jeo.data.dir;

import io.jeo.data.Driver;
import io.jeo.util.Util;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Collection of files with same basename.
 */
public class FileGroup {

    Driver driver;
    List<File> files = new ArrayList<>();

    public FileGroup(File... files) {
        add(files);
    }

    public List<File> files() {
        return files;
    }

    public Driver driver() {
        return driver;
    }

    public FileGroup driver(Driver driver) {
        this.driver = driver;
        return this;
    }

    public FileGroup main(File file) {
        int i = files.indexOf(file);
        if (i == -1) {
            throw new IllegalArgumentException("File: " + file + " is not in group");
        }

        if (i == 0) {
            return this;
        }

        files.remove(i);
        files.add(0, file);
        return this;
    }

    public File main() {
        if (files.isEmpty()) {
            throw new IllegalStateException("File group is empty");
        }
        return files.get(0);
    }

    public String basename() {
        return Util.base(main().getName());
    }

    public FileGroup add(File... files) {
        for (File f : files) {
            this.files.add(f);
        }
        return this;
    }


}
