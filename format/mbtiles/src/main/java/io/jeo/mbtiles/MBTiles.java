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
package io.jeo.mbtiles;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import io.jeo.data.FileDriver;

/**
 * Driver for the MBTiles format, that utilizes SQLite capabilities.
 *
 * @author Justin Deoliveira, OpenGeo
 */
public class MBTiles extends FileDriver<MBTileSet> {

    public static MBTileSet open(File file){
        return new MBTileSet(file);
    }

    @Override
    public String name() {
        return "MBTiles";
    }

    @Override
    public List<String> aliases() {
        return Arrays.asList("mbt");
    }

    @Override
    public Class<MBTileSet> type() {
        return MBTileSet.class;
    }

    @Override
    public MBTileSet open(File file, Map<?, Object> opts) throws IOException {
        return new MBTileSet(file);
    }
}