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
package io.jeo.solr;

import io.jeo.filter.BooleanFilterAdapter;
import io.jeo.filter.Comparison;
import io.jeo.filter.Expression;
import io.jeo.filter.Id;
import io.jeo.filter.Literal;
import io.jeo.filter.Logic;
import io.jeo.filter.Spatial;
import io.jeo.vector.Field;
import io.jeo.vector.Schema;

public class SolrQueryQualifier extends BooleanFilterAdapter  {

    Schema schema;

    SolrQueryQualifier(Schema schema) {
        this.schema = schema;
    }

    @Override
    public Boolean visit(Id<?> id, Object obj) {
        for (Expression e : id.ids()) {
            if (!(e instanceof Literal)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Boolean visit(Logic<?> logic, Object obj) {
        switch(logic.type()) {
            case AND:
                return true;
        }
        return false;
    }

    @Override
    public Boolean visit(Comparison<?> compare, Object obj) {
        compare = compare.normalize();
        if (compare == null || !(compare.right() instanceof Literal)) {
            return false;
        }

        return schema.field(compare.property().property()) != null;
    }

    @Override
    public Boolean visit(Spatial<?> spatial, Object obj) {
        SpatialType type = null;

        spatial = spatial.normalize();
        if (spatial != null && spatial.right() instanceof Literal) {
            Field fld = schema.field(spatial.property().property());
            if (fld != null) {
                type = fld.property(SolrDataset.SPATIAL_TYPE, SpatialType.class);
            }
        }

        if (type == null || type == SpatialType.OTHER) {
            return false;
        }

        switch(type) {
            case RPT:
                switch(spatial.type()) {
                    case INTERSECTS:
                    case CONTAINS:
                    case WITHIN:
                    case DWITHIN:
                        return true;
                    default:
                        return false;
                }

            case BBOX:
                // check shape? bbox only supports rectangles
                switch(spatial.type()) {
                    case BBOX:
                    case CONTAINS:
                    case DISJOINT:
                    case EQUALS:
                    case INTERSECTS:
                    case WITHIN:
                    case DWITHIN:
                        return true;
                    default:
                        return false;
                }
            case POINT:
                // check shape? point only supports rectangles and circles
                switch(spatial.type()) {
                    case INTERSECTS:
                    case WITHIN:
                    case DWITHIN:
                        return true;
                    default:
                        return false;
                }

            default:
                return false;
        }

    }
}
