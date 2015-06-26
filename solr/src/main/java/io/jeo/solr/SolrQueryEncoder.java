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

import io.jeo.filter.Comparison;
import io.jeo.filter.Expression;
import io.jeo.filter.Filter;
import io.jeo.filter.Id;
import io.jeo.filter.Logic;
import io.jeo.filter.Property;
import io.jeo.filter.Spatial;
import io.jeo.filter.StrictFilterAdapter;
import org.apache.solr.client.solrj.SolrQuery;

import java.util.List;
import java.util.Locale;

import static java.lang.String.format;

public class SolrQueryEncoder extends StrictFilterAdapter<Object> {

    SolrQuery q;
    SolrDataset dataset;

    public SolrQueryEncoder(SolrQuery q, SolrDataset dataset) {
        this.q = q;
        this.dataset = dataset;
    }

    @Override
    public Object visit(Id<?> id, Object obj) {
        StringBuilder sb = new StringBuilder();
        for (Expression expr : id.ids()) {
            sb.append(expr.evaluate(null)).append(" OR ");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length()-4);
        }

        q.addFilterQuery(format(Locale.ROOT, "%s:(%s)", dataset.key, sb.toString()));
        return q;
    }

    @Override
    public Object visit(Comparison<?> compare, Object obj) {
        compare = compare.normalize();

        String field = ((Property)compare.left()).property();
        Object value = compare.right().evaluate(null);
        String template;

        Comparison.Type t = compare.type();
        switch(t) {
            case EQUAL:
                template = "%s:%s";
                break;
            case NOT_EQUAL:
                template = "(*:* AND -%s:%s)";
                break;
            case LESS:
                template = "%s:[* TO %s}";
                break;
            case LESS_OR_EQUAL:
                template = "%s:[* TO %s]";
                break;
            case GREATER:
                template = "%s:{%s TO *]";
                break;
            case GREATER_OR_EQUAL:
                template = "%s:[%s TO *]";
                break;
            default:
                throw new IllegalArgumentException("Unsupported comparison filter: " + t);
        }

        q.addFilterQuery(format(Locale.ROOT, template, field, value));
        return q;
    }

    @Override
    public Object visit(Spatial<?> spatial, Object obj) {
        spatial = spatial.normalize();

        String field = ((Property)spatial.left()).property();
        Object literal = spatial.right().evaluate(null);

        String op;
        switch(spatial.type()) {
            case BBOX:
                op = "BBoxIntersects";
                break;
            case EQUALS:
                op = "Equals";
                break;
            case INTERSECTS:
                op = "Intersects";
                break;
            case DISJOINT:
                op = "Disjoint";
                break;
            case WITHIN:
                op = "Within";
                break;
            case CONTAINS:
                op = "Contains";
                break;
            case DWITHIN:
                throw new UnsupportedOperationException("todo");
            default:
                throw new IllegalArgumentException("Unsupported spatial filter: " + spatial.type());
        }

        q.addFilterQuery(format(Locale.ROOT, "%s:\"%s(%s)\"", field, op, literal));
        return q;
    }

    @Override
    public Object visit(Logic<?> logic, Object obj) {
        switch(logic.type()) {
            case AND:
                for (Filter f : logic.parts()) {
                    f.accept(this, obj);
                }
                break;
            default:
                throw new IllegalArgumentException("Unsupported logic filter: " + logic.type());
        }

        return q;
    }
}
