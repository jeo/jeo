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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Visitor interface for {@link com.vividsolutions.jts.geom.Geometry} objects.
 *
 * @see {@link Geom#visit(Geometry, GeometryVisitor)}
 * @see {@link GeometryAdapter}
 */
public interface GeometryVisitor {

    /**
     * Visits a point.
     */
    void visit(Point point);

    /**
     * Visits a linestring.
     */
    void visit(LineString line);

    /**
     * Visits a polygon.
     */
    void visit(Polygon polygon);

    /**
     * Visits a multi point.
     */
    void visit(MultiPoint multiPoint);

    /**
     * Visits a multi line.
     */
    void visit(MultiLineString multiLine);

    /**
     * Visits a multi polygon.
     */
    void visit(MultiPolygon multiPolygon);

    /**
     * Visits a geometry collection.
     */
    void visit(GeometryCollection collection);

    /**
     * Whether the points of the linestring should be visited.
     */
    boolean descend(LineString line);

    /**
     * Whether the rings of the polygon should be visited.
     */
    boolean descend(Polygon polygon);

    /**
     * Whether the points of the multi point should be visited.
     */
    boolean descend(MultiPoint multiPoint);

    /**
     * Whether the linestrings of the multi linestring should be visited.
     */
    boolean descend(MultiLineString multiLine);

    /**
     * Whether the polygons of the multi polygon should be visited.
     */
    boolean descend(MultiPolygon multiPolygon);

    /**
     * Whether the objects of the geometry collection should be visited.
     */
    boolean descend(GeometryCollection collection);
}
