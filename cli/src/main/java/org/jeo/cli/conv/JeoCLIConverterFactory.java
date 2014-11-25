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
package org.jeo.cli.conv;

import org.jeo.cli.cmd.Dimension;
import org.jeo.filter.Filter;
import org.jeo.map.Style;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.IStringConverterFactory;
import com.vividsolutions.jts.geom.Envelope;

public class JeoCLIConverterFactory implements IStringConverterFactory {

    @Override
    public Class<? extends IStringConverter<?>> getConverter(Class clazz) {
        if (clazz == Envelope.class) {
            return EnvelopeConverter.class;
        }
        if (clazz == Filter.class) {
            return FilterConverter.class;
        }
        if (clazz == CoordinateReferenceSystem.class) {
            return CRSConverter.class;
        }
        if (clazz == Style.class) {
            return StyleConverter.class;
        }
        if (clazz == Dimension.class) {
            return DimensionConverter.class;
        }
        if (clazz == java.util.Map.class) {
            return MapConverter.class;
        }
        return null;
    }

}
