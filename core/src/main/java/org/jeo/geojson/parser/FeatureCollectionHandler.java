package org.jeo.geojson.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jeo.feature.Feature;
import org.jeo.json.parser.ParseException;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

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

        @Override
        public boolean startArray() throws ParseException, IOException {
            return true;
        }

        @Override
        public boolean startObject() throws ParseException, IOException {
            push("feature", new FeatureHandler() {
               @Override
               public boolean endObject() throws ParseException, IOException {
                   super.endObject();

                   //consume the feature node
                   Feature f = FeaturesHandler.this.node.consume("feature", Feature.class).get();
                   if (f != null && f.getCRS() == null) {
                       f.setCRS(crs);
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
