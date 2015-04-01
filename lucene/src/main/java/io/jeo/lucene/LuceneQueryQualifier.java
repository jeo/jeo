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
package org.jeo.lucene;

import org.apache.lucene.spatial.SpatialStrategy;
import org.apache.lucene.spatial.bbox.BBoxStrategy;
import org.apache.lucene.spatial.prefix.RecursivePrefixTreeStrategy;
import org.apache.lucene.spatial.vector.PointVectorStrategy;
import io.jeo.filter.BooleanFilterAdapter;
import io.jeo.filter.Comparison;
import io.jeo.filter.Literal;
import io.jeo.filter.Property;
import io.jeo.filter.Spatial;
import io.jeo.filter.Spatial.Type;
import io.jeo.util.Convert;
import io.jeo.vector.Field;

public class LuceneQueryQualifier extends BooleanFilterAdapter {

    LuceneDataset dataset;

    public LuceneQueryQualifier(LuceneDataset dataset) {
        this.dataset = dataset;
    }

    @Override
    public Boolean visit(Property property, Object obj) {
        return true;
    }

    @Override
    public Boolean visit(Literal literal, Object obj) {
        return true;
    }

    @Override
    public Boolean visit(Comparison<?> compare, Object obj) {
        compare = compare.normalize();
        if (compare == null || !(compare.right() instanceof Literal)) {
            return false;
        }

        return true;
    }

    @Override
    public Boolean visit(Spatial<?> spatial, Object obj) {
        spatial = spatial.normalize();
        if (spatial == null || !(spatial.right() instanceof Literal)) {
            return false;
        }

        Field fld = dataset.schema().field(((Property)spatial.left()).property());
        if (fld == null) {
            return false;
        }

        if (!Convert.toGeometry(spatial.right().evaluate(null)).isPresent()) {
            return false;
        }

        Spatial.Type t = spatial.type();
        SpatialStrategy strategy = dataset.spatialField(fld).index.strategy();
        if (strategy instanceof PointVectorStrategy) {
            return t == Type.INTERSECTS || t == Type.WITHIN;
        }
        else if (strategy instanceof BBoxStrategy) {
            switch(t) {
                case BBOX:
                case CONTAINS:
                case INTERSECTS:
                case EQUALS:
                case DISJOINT:
                case WITHIN:
                    return true;
                default:
                    return false;
            }
        }
        else if (strategy instanceof RecursivePrefixTreeStrategy) {

        }
        else {
            return false;
        }

        switch(spatial.type()) {
            case TOUCHES:
            case CROSSES:
            case CONTAINS:
                return false;
        }

        return true;
    }
}
