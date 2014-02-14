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
package org.jeo.feature;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import org.jeo.geom.Geom;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class BasicFeatureTest {

    @Test
    public void testEquals() throws Exception {
        Schema s1 = Schema.build("widgets").fields("name:String,cost:Double,geom:Point").schema();
        BasicFeature f1 = 
            new BasicFeature("1", (List)Lists.newArrayList("bomb", 1.99, Geom.point(0,0)), s1);

        Schema s2 = Schema.build("widgets").fields("name:String,cost:Double,geom:Point").schema();

        Map<String,Object> map = Maps.newLinkedHashMap();
        map.put("name", "bomb");
        map.put("cost", 1.99);
        map.put("geom", Geom.point(0,0));
        
        BasicFeature f2 = new BasicFeature("1", map, s2);

        assertEquals(f1, f2);
    }
}
