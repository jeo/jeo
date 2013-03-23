package org.jeo.geotools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.geotools.feature.simple.SimpleFeatureImpl;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.filter.identity.FeatureIdImpl;
import org.geotools.referencing.CRS;
import org.jeo.data.Cursor;
import org.jeo.feature.Feature;
import org.jeo.feature.Field;
import org.jeo.feature.Schema;
import org.jeo.proj.Proj;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GT {

    static Logger LOGGER = LoggerFactory.getLogger(GT.class);

    public static Feature feature(SimpleFeature feature) {
        return feature(feature, schema(feature.getType()));
    }

    public static Feature feature(SimpleFeature feature, Schema schema) {
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

        if (schema.crs() != null) {
            b.setSRS("EPSG:" + Proj.epsgCode(schema.crs()));
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
}

