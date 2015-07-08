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
package io.jeo.geotools;

import io.jeo.TestData;
import io.jeo.Tests;
import io.jeo.util.Function;
import io.jeo.vector.BasicFeature;
import io.jeo.vector.Feature;
import io.jeo.vector.Schema;
import io.jeo.vector.SchemaBuilder;
import io.jeo.vector.VectorApiTestBase;
import io.jeo.vector.VectorDataset;
import org.geotools.data.DataStore;
import org.geotools.data.DataUtilities;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.DefaultFeatureCollection;
import org.junit.BeforeClass;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import static io.jeo.vector.VectorQuery.all;

public class ShapefileApiTest extends VectorApiTestBase {

    static File shp;

    @BeforeClass
    public static void createShp() throws IOException {
        shp = new File(Tests.newTmpDir(), "states.shp");

        ShapefileDataStoreFactory factory = new ShapefileDataStoreFactory();
        DataStore data = factory.createNewDataStore((Map) Collections.singletonMap("url", DataUtilities.fileToURL(shp)));

        VectorDataset dataset = TestData.states();

        // gt shapefile requires geometry named "the_geom", so we have to hack schema and features on the way in
        final Schema schema =
            SchemaBuilder.renameFields(dataset.schema(), Collections.singletonMap("geometry", "the_geom"));
        SimpleFeatureType type = GT.featureType(schema);
        data.createSchema(type);

        DefaultFeatureCollection features = new DefaultFeatureCollection();
        for (Feature f : dataset.read(all()).map(new Function<Feature, Feature>() {
            @Override
            public Feature apply(Feature f) {
                Map<String,Object> map = f.map();
                map.put("the_geom", map.get("geometry"));
                map.remove("geometry");
                return new BasicFeature(f.id(), map, schema);
            }
        })) {
            features.add(GT.feature(f, type));
        }

        SimpleFeatureStore store = (SimpleFeatureStore) data.getFeatureSource("states");
        store.addFeatures(features);

        data.dispose();
    }

    @Override
    protected VectorDataset createVectorData() throws Exception {
        return Shapefile.open(shp).get("states");
    }
}
