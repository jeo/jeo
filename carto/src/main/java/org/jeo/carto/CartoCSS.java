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
package org.jeo.carto;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.jeo.data.FileDriver;
import org.jeo.map.Style;

public class CartoCSS extends FileDriver<Style> {

    @Override
    public String getName() {
        return "CartoCSS";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("carto", "css");
    }

    @Override
    public Class<Style> getType() {
        return Style.class;
    }

    @Override
    protected Style open(File file, Map<?, Object> opts) throws IOException {
        return Carto.parse(file);
    }

}
