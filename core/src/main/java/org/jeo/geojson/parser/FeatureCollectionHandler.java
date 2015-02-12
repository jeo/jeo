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
package org.jeo.geojson.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jeo.vector.BasicFeature;
import org.jeo.vector.Feature;
import org.jeo.json.parser.ParseException;
import org.osgeo.proj4j.CoordinateReferenceSystem;

public class FeatureCollectionHandler extends BaseHandler {

    CoordinateReferenceSystem crs;
    boolean streaming = true;

    public void setStreaming(boolean streaming) {
        this.streaming = streaming;
    }

    public boolean isStreaming() {
        return streaming;
    }

    @Override
    public boolean startObject() throws ParseException, IOException {
        return true;
    }

    @Override
    public boolean startObjectEntry(String key) throws ParseException, IOException {
        if ("type".equals(key)) {
            push(key, new TypeHandler());
        }
        if ("crs".equals(key)) {
            push(key, new CRSHandler() {
                @Override
                public boolean endObject() throws ParseException, IOException {
                    super.endObject();
                    crs = FeatureCollectionHandler.this.node.consume(
                        "crs", CoordinateReferenceSystem.class).or(null);
                    return true;
                }
            });
        }
        if ("bbox".equals(key)) {
            push(key, new BBOXHandler());
        }
        if ("features".equals(key)) {
            push(key, new FeaturesHandler());
        }
        return true;
    }

    @Override
    public boolean endObjectEntry() throws ParseException, IOException {
        return true;
    }

    public boolean endObject() throws ParseException ,IOException {
        return true;
    }

    class FeaturesHandler extends BaseHandler {

        List<Feature> features = !streaming ? new ArrayList<Feature>() : null;
        int count = 0;

        @Override
        public boolean startArray() throws ParseException, IOException {
            return true;
        }

        @Override
        public boolean startObject() throws ParseException, IOException {
            push("feature", new FeatureHandler(count++) {
               @Override
               public boolean endObject() throws ParseException, IOException {
                   super.endObject();

                   //consume the feature node
                   BasicFeature f = FeaturesHandler.this.node.consume("feature", BasicFeature.class).get();
                   if (f != null && f.crs() == null) {
                       f.crs(crs);
                   }
                   if (!streaming) {
                       features.add(f);
                   }

                   return !streaming;
               } 
            });
            return true;
        }

        @Override
        public boolean endArray() throws ParseException, IOException {
            if (!streaming) {
                node.setValue(features);
            }
            pop();
            return true;
        }
    }
}
