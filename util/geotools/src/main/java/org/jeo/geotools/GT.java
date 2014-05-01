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
package org.jeo.geotools;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.List;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.simple.SimpleFeatureImpl;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.filter.expression.InternalVolatileFunction;
import org.geotools.filter.identity.FeatureIdImpl;
import org.geotools.referencing.CRS;
import org.jeo.data.Cursor;
import org.jeo.feature.Feature;
import org.jeo.feature.Field;
import org.jeo.feature.Schema;
import org.jeo.filter.FilterVisitor;
import org.jeo.filter.Function;
import org.jeo.filter.Literal;
import org.jeo.filter.Mixed;
import org.jeo.filter.Property;
import org.jeo.geotools.render.GTRenderer;
import org.jeo.geotools.render.GTRendererFactory;
import org.jeo.map.View;
import org.jeo.proj.Proj;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.capability.FunctionName;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.ExpressionVisitor;
import org.opengis.filter.expression.VolatileFunction;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GT {

    static Logger LOGGER = LoggerFactory.getLogger(GT.class);

    static FilterFactory filterFactory = CommonFactoryFinder.getFilterFactory();

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
        return new SimpleFeatureImpl(feature.list(), featureType, new FeatureIdImpl(feature.getId()));
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
        b.setName(schema.getName());

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
            b.add(f.getName(), f.getType());
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

    public static Expression expr(org.jeo.filter.Expression expr) {
        Expression e = (Expression) expr.accept(new FilterVisitor() {
            @Override
            public Object visit(Literal literal, Object obj) {
                return filterFactory.literal(literal.evaluate(null));
            }

            @Override
            public Object visit(Property property, Object obj) {
                return filterFactory.property(property.getProperty());
            }

            @Override
            public Object visit(Mixed mixed, Object obj) {
                List<Expression> l = new ArrayList<Expression>();
                for (org.jeo.filter.Expression e : mixed.getExpressions()) {
                    l.add(expr(e));
                }

                return filterFactory.function("strConcat", l.toArray(new Expression[l.size()]));
            }

            @Override
            public Object visit(final Function function, Object obj) {
                return new InternalVolatileFunction(function.getName()) {
                    @Override
                    public Object evaluate(Object object) {
                        if (object instanceof SimpleFeature) {
                            return function.evaluate(feature((SimpleFeature)object));
                        }
                        return function.evaluate(object);
                        //throw new IllegalArgumentException(
                        //    "unable to handle function input: " + object);
                    }
                };
            }

        }, null);

        if (e == null) {
            throw new IllegalArgumentException("unable to convert: " + expr);
        }

        return e;
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

