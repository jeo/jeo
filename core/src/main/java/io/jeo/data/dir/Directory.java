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

import io.jeo.data.FileDriver;
import io.jeo.util.Messages;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Driver for directory of files.
 * <p>
 * Works by delegating to other drivers capable of reading/writing individual file types.
 * </p>
 */
public class Directory extends FileDriver<DirWorkspace> {

    public static DirWorkspace open(File file) {
        return new DirWorkspace(file);
    }

    @Override
    public String name() {
        return "Directory";
    }

    @Override
    public Class<DirWorkspace> type() {
        return DirWorkspace.class;
    }

    @Override
    protected boolean canOpen(File file, Map<?, Object> opts, Messages msgs) {
        if (!file.isDirectory()) {
            Messages.of(msgs).report("File must be a directory: " + file.getPath());
            return false;
        }

        return super.canOpen(file, opts, msgs);
    }

    @Override
    protected DirWorkspace open(File file, Map<?, Object> opts) throws IOException {
        return new DirWorkspace(file);
    }
}
