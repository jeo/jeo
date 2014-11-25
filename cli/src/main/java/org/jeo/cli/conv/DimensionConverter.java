/* Copyright 2014 The jeo project. All rights reserved.
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
package org.jeo.cli.conv;

import org.jeo.cli.cmd.Dimension;

import com.beust.jcommander.IStringConverter;

public class DimensionConverter implements IStringConverter<Dimension> {

    @Override
    public Dimension convert(String value) {
        String[] split = value.split(" *, *");
        Integer width = null;
        Integer height = null;

        if (split.length == 1) {
            width = height = Integer.parseInt(split[0]);
        }
        else {
            width = !split[0].isEmpty() ? Integer.parseInt(split[0]) : null;
            height = !split[1].isEmpty() ? Integer.parseInt(split[0]) : null;

            if (width == null && height == null) {
                throw new IllegalArgumentException("Dimension must specify either width or height");
            }

            width = width != null ? width : height;
            height = height != null ? height : width;
        }

        return new Dimension(width, height);
    }

}
