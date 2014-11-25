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

import java.util.Random;

import org.jeo.filter.Function;
import org.jeo.map.RGB;

/**
 * Function that evaluates to a random color.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class RandColorFunction extends Function {

    Random r;
    
    public RandColorFunction() {
        super("randcolor");
        r = new Random();
    }
    
    @Override
    public Object evaluate(Object obj) {
        return new RGB(r.nextInt(255), r.nextInt(255), r.nextInt(255));
    }
}
