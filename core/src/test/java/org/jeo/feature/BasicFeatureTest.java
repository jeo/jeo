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
