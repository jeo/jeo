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

import com.beust.jcommander.IStringConverter;
import com.vividsolutions.jts.geom.Envelope;

public class EnvelopeConverter implements IStringConverter<Envelope> {

    @Override
    public Envelope convert(String str) {
        String[] split = str.split(",");
        if (split.length != 4) {
            throw new IllegalArgumentException("bbox syntax: <xmin,ymin,xmax,ymax>");
        }

        return new Envelope(Double.parseDouble(split[0]), Double.parseDouble(split[2]), 
            Double.parseDouble(split[1]), Double.parseDouble(split[3]));
    }

}
