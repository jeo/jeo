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
package io.jeo.geom;

import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Adapter for {@link GeometryVisitor}.
 * <p>
 * This base class does not descend by default. To cause it to decend either use the {@link #GeometryAdapter(boolean)}
 * constructor to enable descend for all objects, or override the individual methods.
 * </p>
 */
public class GeometryAdapter implements GeometryVisitor {

    boolean decend;

    public GeometryAdapter() {
        this(false);
    }

    public GeometryAdapter(boolean decend) {
        this.decend = decend;
    }

    @Override
    public void visit(Point point) {
    }

    @Override
    public void visit(LineString line) {
    }

    @Override
    public void visit(Polygon polygon) {
    }

    @Override
    public void visit(MultiPoint multiPoint) {
    }

    @Override
    public void visit(MultiLineString multiLine) {
    }

    @Override
    public void visit(MultiPolygon multiPolygon) {
    }

    @Override
    public void visit(GeometryCollection collection) {
    }

    @Override
    public boolean descend(LineString line) {
        return decend;
    }

    @Override
    public boolean descend(Polygon polygon) {
        return decend;
    }

    @Override
    public boolean descend(MultiPoint multiPoint) {
        return decend;
    }

    @Override
    public boolean descend(MultiLineString multiLine) {
        return decend;
    }

    @Override
    public boolean descend(MultiPolygon multiPolygon) {
        return decend;
    }

    @Override
    public boolean descend(GeometryCollection collection) {
        return decend;
    }
}
