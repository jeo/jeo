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
package org.jeo.filter.cql;

import java.io.StringReader;

import org.jeo.filter.Filter;

public class ECQLBuilder extends ECQLParser {

    CQLHelper h;

    public ECQLBuilder(String cql) {
        super(new StringReader(cql));
        this.h = new CQLHelper(cql);
    }

    public Filter parse() throws ParseException {
        FilterCompilationUnit();
        return h.filter();
    }

    public void jjtreeCloseNodeScope(Node n) throws ParseException {
        onClose((SimpleNode) n);
    }

    void onClose(SimpleNode n) throws ParseException {
        switch(n.getType()) {
        // Literals
        // note, these should never throw because the parser grammar
        // constrains input before we ever reach here!
        case JJTINTEGERNODE:
            h.intLiteral(getToken(0));
            return;
            //return this.builder.buildLiteralInteger(getToken(0).image);

        case JJTFLOATINGNODE:
            h.floatLiteral(getToken(0));
            return;
            
            //return  this.builder.buildLiteralDouble(getToken(0).image);

        case JJTSTRINGNODE:
            h.stringLiteral(getToken(0));
            return;

            //return this.builder.buildLiteralString(getToken(0).image);
            // ----------------------------------------
            // Identifier
            // ----------------------------------------
        case JJTIDENTIFIER_NODE:
            return;
            //return this.builder.buildIdentifier(JJTIDENTIFIER_PART_NODE);

        case JJTIDENTIFIER_PART_NODE:
            h.idPart(getToken(0));
            return;
            //return this.builder.buildIdentifierPart(getTokenInPosition(0));

            // ----------------------------------------
            // attribute
            // ----------------------------------------
        case JJTSIMPLE_ATTRIBUTE_NODE:
            h.property(); 
            return;
             
            //return this.builder.buildSimpleAttribute();

        case JJTCOMPOUND_ATTRIBUTE_NODE:
            return;
            //return this.builder.buildCompoundAttribute(JJTSIMPLE_ATTRIBUTE_NODE, ATTRIBUTE_PATH_SEPARATOR);

            // ----------------------------------------
            // function
            // ----------------------------------------
        case JJTFUNCTION_NODE:
            return;
            //return this.builder.buildFunction(JJTFUNCTIONNAME_NODE);

        case JJTFUNCTIONNAME_NODE:
            return;
            //return cqlNode; // used as mark of function name in stack

        case JJTFUNCTIONARG_NODE:
            return;
            //return cqlNode; // used as mark of args in stack

            // Math Nodes
        case JJTADDNODE:
            h.add();
            return;
        case JJTSUBTRACTNODE:
            h.subtract();
            return;
        case JJTMULNODE:
            h.multiply();
            return;
        case JJTDIVNODE:
            h.divide();
            return;
            //return buildBinaryExpression(cqlNode.getType());

            // Boolean expression
        case JJTBOOLEAN_AND_NODE:
            h.and();
            return;
            //return buildLogicFilter(JJTBOOLEAN_AND_NODE);

        case JJTBOOLEAN_OR_NODE:
            h.or();
            return;
            //return buildLogicFilter(JJTBOOLEAN_OR_NODE);

        case JJTBOOLEAN_NOT_NODE:
            h.not();
            return;
            //return buildLogicFilter(JJTBOOLEAN_NOT_NODE);

        case JJTIN_PREDICATE_NODE:
            h.in();
            return;
        case JJTNOT_IN_PREDICATE_NODE:
            h.notIn();
            return;

            // ----------------------------------------
            // between predicate actions
            // ----------------------------------------
        case JJTBETWEEN_NODE:
            h.between();
            //return this.builder.buildBetween();

        case JJTNOT_BETWEEN_NODE:
            return;
            //return this.builder.buildNotBetween();

            // ----------------------------------------
            // Compare predicate actions
            // ----------------------------------------
        case JJTFEATURE_ID_NODE:
            h.fidLiteral(getToken(0));
            return;

        case JJTID_PREDICATE_NODE:
            h.id(); 
            return;
            
        case JJTCOMPARISONPREDICATE_EQ_NODE:
            h.eq();
            return;
        case JJTCOMPARISONPREDICATE_GT_NODE:
            h.gt();
            return;
        case JJTCOMPARISONPREDICATE_LT_NODE:
            h.lt();
            return;
        case JJTCOMPARISONPREDICATE_GTE_NODE:
            h.gte();
            return;
        case JJTCOMPARISONPREDICATE_LTE_NODE:
            h.lte();
            return;
            //return buildBinaryComparasionOperator(cqlNode.getType());

        case JJTCOMPARISONPREDICATE_NOT_EQUAL_NODE:
            h.neq();
            return;

            //Filter eq = buildBinaryComparasionOperator(JJTCOMPARISONPREDICATE_EQ_NODE);
            //Not notFilter = this.builder.buildNotFilter(eq);

            //return notFilter;

            // ----------------------------------------
            // Text predicate (Like)
            // ----------------------------------------
        case JJTLIKE_NODE:
            h.like();
            return;
            //return this.builder.buildLikeFilter(true);

        case JJTNOT_LIKE_NODE:
            h.notLike();
            return;
            //return this.builder.buildNotLikeFilter(true);

            // ----------------------------------------
            // Null predicate
            // ----------------------------------------
        case JJTNULLPREDICATENODE:
            return;
            //return this.builder.buildPropertyIsNull();

        case JJTNOTNULLPREDICATENODE:
            return;
            //return this.builder.buildPorpertyNotIsNull();

            // ----------------------------------------
            // temporal predicate actions
            // ----------------------------------------
        case JJTDATETIME_NODE:
            return;
            //return this.builder.buildDateTimeExpression(getTokenInPosition(0));

        case JJTDURATION_DATE_NODE:
            return;
            //return this.builder.buildDurationExpression(getTokenInPosition(0));

        case JJTPERIOD_BETWEEN_DATES_NODE:
            return;
            //return this.builder.buildPeriodBetweenDates();

        case JJTPERIOD_WITH_DATE_DURATION_NODE:
            return;
            //return this.builder.buildPeriodDateAndDuration();

        case JJTPERIOD_WITH_DURATION_DATE_NODE:
            return;
            //return this.builder.buildPeriodDurationAndDate();

        case JJTTPBEFORE_DATETIME_NODE:
            return;
            //return buildBeforePredicate();

        case JJTTPAFTER_DATETIME_NODE:
            return;
            //return buildAfterPredicate();

        case JJTTPDURING_PERIOD_NODE:
            return;
            //return buildDuring();

        case JJTTPBEFORE_OR_DURING_PERIOD_NODE:
            return;
            //return buildBeforeOrDuring();

        case JJTTPDURING_OR_AFTER_PERIOD_NODE:
            return;
            //return buildDuringOrAfter();

            // ----------------------------------------
            // existence predicate actions
            // ----------------------------------------
        case JJTEXISTENCE_PREDICATE_EXISTS_NODE:
            return;
            //return this.builder.buildPropertyExists();

        case JJTEXISTENCE_PREDICATE_DOESNOTEXIST_NODE:
            return;

            //Filter filter = this.builder.buildPropertyExists();
            //Filter filterPropNotExist = this.builder.buildNotFilter(filter);

            //return filterPropNotExist;

            // ----------------------------------------
            // routine invocation Geo Operation
            // -------------------TokenAdapter.newAdapterFor(cqlNode.getToken())---------------------
        case JJTROUTINEINVOCATION_GEOOP_EQUAL_NODE:
            h.equals();
            return;
        case JJTROUTINEINVOCATION_GEOOP_DISJOINT_NODE:
            h.disjoint();
            return;
        case JJTROUTINEINVOCATION_GEOOP_INTERSECT_NODE:
            h.intersect();
            return;
        case JJTROUTINEINVOCATION_GEOOP_TOUCH_NODE:
            h.touch();
            return;
        case JJTROUTINEINVOCATION_GEOOP_CROSS_NODE:
            h.cross();
            return;
        case JJTROUTINEINVOCATION_GEOOP_WITHIN_NODE:
            h.within();
            return;
        case JJTROUTINEINVOCATION_GEOOP_CONTAIN_NODE:
            h.contain();
            return;
        case JJTROUTINEINVOCATION_GEOOP_OVERLAP_NODE:
            h.overlap();
            return;
            //return buildBinarySpatialOperator(cqlNode.getType());

        case JJTROUTINEINVOCATION_GEOOP_BBOX_NODE:
            h.bbox();
            return;
        case JJTROUTINEINVOCATION_GEOOP_BBOX_SRS_NODE:
            h.bboxWithSRS();
            return;

        case JJTROUTINEINVOCATION_GEOOP_RELATE_NODE:
            //    return this.builder.buildSpatialRelateFilter();
                
        case JJTDE9IM_NODE:
            //    return this.builder.buildDE9IM( getToken(0).image) ;    
                
            // ----------------------------------------
            // routine invocation RelGeo Operatiosn
            // ----------------------------------------
        case JJTTOLERANCE_NODE:
            //return this.builder.buildTolerance();

        case JJTDISTANCEUNITS_NODE:
            //return this.builder.buildDistanceUnit(getTokenInPosition(0));

        case JJTROUTINEINVOCATION_RELOP_BEYOND_NODE:
        case JJTROUTINEINVOCATION_RELOP_DWITHIN_NODE:
            //return buildDistanceBufferOperator(cqlNode.getType());

            // ----------------------------------------
            // Geometries:
            // ----------------------------------------
        case JJTWKTNODE:
            try {
                h.wktLiteral(n.getToken());
                return;
            } catch (com.vividsolutions.jts.io.ParseException e) {
                throw (ParseException) new ParseException().initCause(e);
            }
            //return this.builder.buildGeometry(TokenAdapter.newAdapterFor(cqlNode.getToken()));

        case JJTENVELOPETAGGEDTEXT_NODE:
            //return this.builder.buildEnvelop(TokenAdapter.newAdapterFor(cqlNode.getToken()));

        case JJTINCLUDE_NODE:
            //return Filter.INCLUDE;

        case JJTEXCLUDE_NODE:
            //return Filter.EXCLUDE;

        case JJTTRUENODE:
            //return this.builder.buildTrueLiteral();

        case JJTFALSENODE:
            //return this.builder.buildFalseLiteral(); 
        }

    };
}
