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
package io.jeo.vector;

import com.vividsolutions.jts.geom.Point;
import io.jeo.geom.Geom;
import io.jeo.data.Cursors;
import io.jeo.proj.Proj;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class FeatureCursorTest {

    @Test
    public void testCrs() throws Exception {
        Feature f = new ListFeature(Schema.build("test").field("geo", Point.class).schema(), Geom.point(0, 0));

        assertNull(Features.crs(f));
        assertNull(Features.schema("feature", f).crs());

        f = FeatureCursor.wrap(Cursors.single(f)).crs(Proj.EPSG_900913).first().get();

        assertEquals(Proj.EPSG_900913, Features.crs(f));
        assertEquals(Proj.EPSG_900913, Features.schema("feature", f).crs());
    }
}
