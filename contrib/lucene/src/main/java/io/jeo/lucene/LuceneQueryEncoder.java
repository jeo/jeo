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
package io.jeo.lucene;

import com.spatial4j.core.shape.Shape;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.spatial.SpatialStrategy;
import org.apache.lucene.spatial.bbox.BBoxStrategy;
import org.apache.lucene.spatial.prefix.RecursivePrefixTreeStrategy;
import org.apache.lucene.spatial.query.SpatialArgs;
import org.apache.lucene.spatial.query.SpatialOperation;
import org.apache.lucene.spatial.vector.PointVectorStrategy;
import io.jeo.filter.Comparison;
import io.jeo.filter.Comparison.Type;
import io.jeo.filter.Filter;
import io.jeo.filter.Literal;
import io.jeo.filter.Logic;
import io.jeo.filter.Property;
import io.jeo.filter.Spatial;
import io.jeo.filter.StrictFilterAdapter;
import io.jeo.util.Convert;
import io.jeo.util.Optional;
import io.jeo.vector.Field;
import io.jeo.vector.Schema;

import java.math.BigInteger;

public class LuceneQueryEncoder extends StrictFilterAdapter<Object> {

    public static Query encode(Filter filter, LuceneDataset dataset) {
        LuceneQueryEncoder encoder = new LuceneQueryEncoder(dataset);
        return (Query) filter.accept(encoder, null);
    }

    Schema schema;
    LuceneDataset dataset;
    QueryParser parser;

    public LuceneQueryEncoder(LuceneDataset dataset) {
        this.dataset = dataset;
        schema = dataset.schema();
        parser = new QueryParser("title", dataset.analyzer());
    }

    @Override
    public Object visit(Property property, Object obj) {
        return ((StringBuilder)obj).append(property.property());
    }

    @Override
    public Object visit(Literal literal, Object obj) {
        return ((StringBuilder)obj).append(literal.evaluate(null));
    }

    @Override
    public Object visit(Logic<?> logic, Object obj) {
        Logic.Type t = logic.type();

        Occur occur = t == Logic.Type.AND ? Occur.MUST : t == Logic.Type.OR ? Occur.SHOULD : Occur.MUST_NOT;

        BooleanQuery q = new BooleanQuery();

        // special case for NOT, need to AND with Match all docs query
        if (t == Logic.Type.NOT) {
            q.add(new MatchAllDocsQuery(), Occur.SHOULD);
        }

        for (Filter f : logic.parts()) {
            q.add((Query)f.accept(this, q), occur);
        }
        return q;
    }

    @Override
    public Object visit(Comparison<?> compare, Object obj) {
        compare = compare.normalize();

        Comparison.Type t = compare.type();
        if (t != Type.EQUAL && t != Type.NOT_EQUAL) {
            Property field = (Property) compare.left();

            // range query, special case for numeric range
            Optional<Number> num = Convert.toNumber(compare.right().evaluate(null));
            if (num.isPresent()) {
                Number n = num.get();
                Number n1 = n;
                Number n2 = null;
                boolean min = true, max = true;
                switch(t) {
                    case LESS:
                        max = false;
                    case LESS_OR_EQUAL:
                        n2 = n1;
                        n1 = null;
                        break;
                    case GREATER:
                        min = false;
                        break;
                }

                NumericRangeQuery<? extends Number> rq;
                if (n instanceof Integer || n instanceof Short || n instanceof Byte) {
                    Integer i1 = n1 != null ? n1.intValue() : null;
                    Integer i2 = n2 != null ? n2.intValue() : null;
                    rq = NumericRangeQuery.newIntRange(field.property(), i1 , i2, min, max);
                }
                else if (n instanceof Long || n instanceof BigInteger) {
                    Long l1 = n1 != null ? n1.longValue() : null;
                    Long l2 = n2 != null ? n2.longValue() : null;
                    rq = NumericRangeQuery.newLongRange(field.property(), l1, l2, min, max);
                }
                else if (n instanceof Float) {
                    Float f1 = n1 != null ? n1.floatValue() : null;
                    Float f2 = n2 != null ? n2.floatValue() : null;
                    rq = NumericRangeQuery.newFloatRange(field.property(), f1, f2, min, max);
                }
                else {
                    Double d1 = n1 != null ? n1.doubleValue() : null;
                    Double d2 = n2 != null ? n2.doubleValue() : null;
                    rq = NumericRangeQuery.newDoubleRange(field.property(), d1, d2, min, max);
                }

                return rq;
            }
        }

        StringBuilder buf = new StringBuilder();
        compare.left().accept(this, buf);
        buf.append(":");

        switch(compare.type()) {
            case EQUAL:
            case NOT_EQUAL:
                compare.right().accept(this, buf);
                break;
            case LESS:
            case LESS_OR_EQUAL:
                buf.append("[* TO ");
                compare.right().accept(this, buf);
                buf.append(t == Type.LESS ? "}" : "]");
                break;
            case GREATER:
            case GREATER_OR_EQUAL:
                buf.append(t == Type.GREATER ? "{" : "[");
                compare.right().accept(this, buf);
                buf.append(" TO *]");
                break;
            default:
                throw new IllegalArgumentException("comparison operator not supported: " + t);
        }


        if (t == Type.NOT_EQUAL) {
            buf.insert(0, "*:* AND -(").append(")");
        }

        try {
            return parser.parse(buf.toString());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public Object visit(Spatial<?> spatial, Object obj) {
        spatial = spatial.normalize();
        Field fld = schema.field(((Property)spatial.left()).property());

        Spatial.Type t = spatial.type();
        SpatialStrategy strategy = dataset.spatialField(fld).index.strategy();

        Geometry geo = Convert.toGeometry(spatial.right().evaluate(null)).get();

        SpatialOperation op = null;
        Shape shp = null;

        if (strategy instanceof PointVectorStrategy) {
            shp = SpatialUtil.rectangle(geo, dataset.ctx());
            switch(t) {
                case INTERSECTS:
                    op = SpatialOperation.Intersects;
                    break;
                case WITHIN:
                    op = SpatialOperation.IsWithin;
                    break;
            }
        }
        else if (strategy instanceof BBoxStrategy) {
            Envelope env = geo.getEnvelopeInternal();
            shp = SpatialUtil.rectangle(geo, dataset.ctx());
            switch(t) {
                case BBOX:
                    op = SpatialOperation.BBoxIntersects;
                    break;
                case CONTAINS:
                    op = SpatialOperation.Contains;
                    break;
                case INTERSECTS:
                    op = SpatialOperation.Intersects;
                    break;
                case EQUALS:
                    op = SpatialOperation.IsEqualTo;
                    break;
                case DISJOINT:
                    op = SpatialOperation.IsDisjointTo;
                    break;
                case WITHIN:
                    op = SpatialOperation.IsWithin;
                    break;
            }
        }
        else if (strategy instanceof RecursivePrefixTreeStrategy) {
            shp = dataset.ctx().makeShape(geo);
            switch(t) {
                case INTERSECTS:
                    op = SpatialOperation.Intersects;
                    break;
                case WITHIN:
                    op = SpatialOperation.IsWithin;
                    break;
                case CONTAINS:
                    op = SpatialOperation.Contains;
                    break;
            }
        }

        if (op == null || shp == null) {
            throw new IllegalStateException();
        }

        return strategy.makeQuery(new SpatialArgs(op, shp));
    }
}
