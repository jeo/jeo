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
package io.jeo.vector;

import java.util.Map;

import io.jeo.geom.Geom;
import org.junit.Test;

import com.google.common.collect.Maps;

import static org.junit.Assert.assertEquals;

public class FeatureTest {

    @Test
    public void testEquals() throws Exception {
        Schema s1 = Schema.build("widgets").fields("name:String,cost:Double,geom:Point").schema();
        ListFeature f1 = new ListFeature("1", s1, "bomb", 1.99, Geom.point(0, 0));

        //Schema s2 = Schema.build("widgets").fields("name:String,cost:Double,geom:Point").schema();

        Map<String,Object> map = Maps.newLinkedHashMap();
        map.put("name", "bomb");
        map.put("cost", 1.99);
        map.put("geom", Geom.point(0,0));
        
        MapFeature f2 = new MapFeature("1", map);

        assertEquals(f1, f2);
    }
}
