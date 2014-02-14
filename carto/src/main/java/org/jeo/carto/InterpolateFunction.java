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

import java.util.List;

import org.jeo.filter.Function;
import org.jeo.filter.Property;
import org.jeo.map.RGB;
import org.jeo.util.Convert;

public class InterpolateFunction extends Function {

    public InterpolateFunction() {
        super("interpolate");
    }

    @Override
    public Object evaluate(Object obj) {
        if (args.size() < 5) {
            String msg = "Invalid number of arguments, " +
                "Usage: interpolate(<property>, <color>, <color>, <value>, <value>)";
            throw new IllegalArgumentException(msg);
        }
        
        Property p = (Property) args.get(0);
        RGB c1 = Convert.to(args.get(1).evaluate(obj), RGB.class, false).get();
        RGB c2 = Convert.to(args.get(2).evaluate(obj), RGB.class, false).get();
        Number n1 = Convert.toNumber(args.get(3).evaluate(obj)).get();
        Number n2 = Convert.toNumber(args.get(4).evaluate(obj)).get();
    
        List<RGB> cols = c1.interpolate(c2, 100, org.jeo.util.Interpolate.Method.LINEAR);
        List<Double> vals = 
            org.jeo.util.Interpolate.linear(n1.doubleValue(), n2.doubleValue(), 100);
    
        Number val = Convert.toNumber(p.evaluate(obj)).get();
        if (val == null) {
            throw new IllegalArgumentException("Property " + p + " evaluated to null");
        }
    
        int i = 0;
        double d = Double.MAX_VALUE;
        for (i = 0; i < vals.size(); i++) {
            double e = Math.abs(vals.get(i) - val.doubleValue());
            if (e < d) {
                d = e;
            }
            else {
                break;
            }
        }
    
        return cols.get(i-1);
    }

}
