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
package org.jeo.lucene;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.shape.Rectangle;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

class SpatialUtil {

    public static Rectangle rectangle(Geometry geo, SpatialContext ctx) {
        return rectangle(geo.getEnvelopeInternal(), ctx);
    }

    public static Rectangle rectangle(Envelope env, SpatialContext ctx) {
        return ctx.makeRectangle(env.getMinX(), env.getMaxX(), env.getMinY(), env.getMaxY());
    }
}
