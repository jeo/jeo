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

import java.util.Map;

import com.beust.jcommander.IStringConverter;
import com.google.common.collect.Maps;

public class MapConverter implements IStringConverter<java.util.Map<String,String>>{

    @Override
    public Map<String, String> convert(String value) {
        Map<String,String> map = Maps.newLinkedHashMap();
        String[] entries = value.split("; *");
        for (String e : entries) {
            String[] kv = e.split(" *= *");
            if (kv.length > 0) {
                map.put(kv[0], kv.length > 1 ? kv[1] : null);
            }
        }
        return map;
    }

}
