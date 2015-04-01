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
package io.jeo.mbtiles;

import java.io.File;
import java.util.Map;
import java.util.LinkedHashMap;
import io.jeo.util.Key;
import static io.jeo.data.FileDriver.FILE;

public class MBTilesOpts {

    File file;

    public static MBTilesOpts fromMap(Map<?,Object> map) {
        return new MBTilesOpts(FILE.get(map));
    }

    public MBTilesOpts(File file) {
        this.file = file;
    }

    public File file() {
        return file;
    }

    public Map<Key<?>,Object> toMap() {
        Map<Key<?>,Object> map = new LinkedHashMap<Key<?>, Object>();
        map.put(FILE, file);
        return map;
    }

}
