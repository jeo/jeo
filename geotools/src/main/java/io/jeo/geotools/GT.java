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
package io.jeo.geotools;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.List;

import com.vividsolutions.jts.geom.Envelope;
import io.jeo.filter.All;
import io.jeo.filter.None;
import io.jeo.util.Supplier;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.Hints;
import org.geotools.feature.simple.SimpleFeatureImpl;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.filter.expression.InternalVolatileFunction;
import org.geotools.filter.identity.FeatureIdImpl;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import io.jeo.data.Cursor;
import io.jeo.filter.FilterWalker;
import io.jeo.vector.Feature;
import io.jeo.vector.Field;
import io.jeo.vector.Schema;
import io.jeo.filter.Function;
import io.jeo.filter.Literal;
import io.jeo.filter.Mixed;
import io.jeo.filter.Property;
import io.jeo.geotools.render.GTRenderer;
import io.jeo.geotools.render.GTRendererFactory;
import io.jeo.map.View;
import io.jeo.proj.Proj;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Expression;
import org.opengis.referencing.FactoryException;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GT {

    static Logger LOGGER = LoggerFactory.getLogger(GT.class);

    static FilterFactory2 filterFactory = CommonFactoryFinder.getFilterFactory2();

    static final String FORCE_XY = "org.geotools.referencing.forceXY";

    /**
     * Configures GeoTools referencing to force longitude/latitude (x/y) ordering.
     */
    public static void initLonLatAxisOrder() {
        if (System.getProperty(FORCE_XY) == null) {
            System.setProperty(FORCE_XY, "true");
        }
        Hints.putSystemDefault(Hints.FORCE_AXIS_ORDER_HONORING, "http");
    }

    /**
     * Configures GeoTools referencing to use a more lax comparison tolerance. This
     * improves the ability to do things like datum matching, which a direct match
     * by name is not found.
     */
    public static void initLaxComparisonTolerance() {
        Hints.putSystemDefault(Hints.COMPARISON_TOLERANCE, 1e-9);
    }

    public static Feature feature(SimpleFeature feature) {
        return feature(feature, schema(feature.getType()));
    }

    public static GTFeature feature(SimpleFeature feature, Schema schema) {
        return new GTFeature(feature, schema);
    }

    public static SimpleFeature feature(Feature feature) {
        return feature(feature, featureType(feature.schema()));
    }

    public static SimpleFeature feature(Feature feature, SimpleFeatureType featureType) {
        return new SimpleFeatureImpl(feature.list(), featureType, new FeatureIdImpl(feature.id()));
    }

    public static Schema schema(SimpleFeatureType featureType) {
        List<Field> fields = new ArrayList<Field>();
        for (AttributeDescriptor ad : featureType.getAttributeDescriptors()) {
            CoordinateReferenceSystem crs = null;
            if (ad instanceof GeometryDescriptor) {
                GeometryDescriptor gd = (GeometryDescriptor) ad;
                if (gd.getCoordinateReferenceSystem() != null) {
                    Integer epsg = null;
                    try {
                        epsg = CRS.lookupEpsgCode(gd.getCoordinateReferenceSystem(), false);
                    } catch (Exception e) {
                        LOGGER.debug("Error looking up epsg code:" + e);
                    }
                    if (epsg != null) {
                        crs = Proj.crs(epsg);
                    }
                }
            }
            fields.add(new Field(ad.getLocalName(), ad.getType().getBinding(), crs));
        }

        return new Schema(featureType.getTypeName(), fields);
    }

    public static SimpleFeatureType featureType(Schema schema) {
        SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();
        b.setName(schema.name());

        Integer epsg = null;
        if (schema.crs() != null) {
            epsg = Proj.epsgCode(schema.crs());
        }
        if (epsg != null) {
            b.setSRS("EPSG:" + epsg);
        }
        else {
            b.setCRS(null);
        }

        for (Field f : schema) {
            b.add(f.name(), f.type());
        }

        return b.buildFeatureType();
    }

    public static Iterator<SimpleFeature> iterator(final Cursor<Feature> cursor) {
        return new Iterator<SimpleFeature>() {

            @Override
            public boolean hasNext() {
                try {
                    boolean next = cursor.hasNext();
                    if (!next) {
                        cursor.close();
                    }
                    return next;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public SimpleFeature next() {
                try {
                    return GT.feature(cursor.next());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
            
        };
    }

    public static Expression expr(final io.jeo.filter.Expression expr) {
        return new FilterConverter(filterFactory, null).convert(expr)
            .orElseThrow(new Supplier<RuntimeException>() {
                @Override
                public RuntimeException get() {
                    throw new IllegalArgumentException("Unable to convert expression: " + expr);
                }
            });
    }

    public static Filter filter(final io.jeo.filter.Filter filter, SimpleFeatureType featureType) {
        return new FilterConverter(filterFactory, featureType).convert(filter)
            .orElseThrow(new Supplier<RuntimeException>() {
                @Override
                public RuntimeException get() {
                    throw new IllegalArgumentException("Unable to convert filter: " + filter);
                }
            });
    }

    public static CoordinateReferenceSystem crs(org.opengis.referencing.crs.CoordinateReferenceSystem crs) {
        Integer epsg = null;
        try {
            epsg = CRS.lookupEpsgCode(crs, false);
        } catch (Exception e) {
            LOGGER.debug("Error looking up epsg code for {}", crs, e);
        }

        return epsg != null ? Proj.crs(epsg) : Proj.crs(crs.toWKT());
    }

    /**
     * Renders the view to a buffered image.
     *
     * @param v The map view.
     *
     * @return The rendered image.
     */
    public static BufferedImage draw(View v) throws IOException {
        BufferedImage img = new BufferedImage(v.getWidth(), v.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);

        Map<Object,Object> opts = new HashMap<Object, Object>();
        opts.put(GTRendererFactory.IMAGE, img);

        GTRenderer r = new GTRendererFactory().create(v, opts);
        r.init(v, null);
        r.render(null);

        return img;
    }

    /**
     * Renders the view into a Frame and shows it.
     *
     * @param v The map view.
     */
    public static void drawAndShow(View v) throws IOException {
        final BufferedImage img = draw(v);

        Frame frame = new Frame();
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                e.getWindow().dispose();
            }
        });

        Panel p = new Panel() {
            {
                setPreferredSize(new Dimension(img.getWidth(), img.getHeight()));
            }

            public void paint(Graphics g) {
                g.drawImage(img, 0, 0, this);
            }
        };

        frame.add(p);
        frame.pack();
        frame.setVisible(true);
    }
}

