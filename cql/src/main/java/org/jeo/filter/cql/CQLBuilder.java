package org.jeo.filter.cql;

import java.io.StringReader;
import java.util.ArrayDeque;
import java.util.Deque;

import org.jeo.filter.Comparison;
import org.jeo.filter.Expression;
import org.jeo.filter.Filter;
import org.jeo.filter.FilterBuilder;

import com.vividsolutions.jts.io.WKTReader;

public class CQLBuilder extends CQLParser {

    FilterBuilder builder = new FilterBuilder(); 
    Deque<Object> stack = new ArrayDeque<Object>();
    String cql;

    public CQLBuilder(String cql) {
        super(new StringReader(cql));
        this.cql = cql;
    }

    public Filter<Object> parse() throws ParseException {
        FilterCompilationUnit();
        return builder.filter();
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
            builder.literal(Integer.parseInt(getToken(0).image)); return;
            //return this.builder.buildLiteralInteger(getToken(0).image);

        case JJTFLOATINGNODE:
            builder.literal(Double.parseDouble(getToken(0).image)); return;
            //return  this.builder.buildLiteralDouble(getToken(0).image);

        case JJTSTRINGNODE:
            builder.literal(getToken(0).image); return;
            //return this.builder.buildLiteralString(getToken(0).image);
            // ----------------------------------------
            // Identifier
            // ----------------------------------------
        case JJTIDENTIFIER_NODE:
            return;
            //return this.builder.buildIdentifier(JJTIDENTIFIER_PART_NODE);

        case JJTIDENTIFIER_PART_NODE:
            stack.push(getToken(0).image); return;
            //return this.builder.buildIdentifierPart(getTokenInPosition(0));

            // ----------------------------------------
            // attribute
            // ----------------------------------------
        case JJTSIMPLE_ATTRIBUTE_NODE:
            builder.property((String)stack.pop()); return;
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
        case JJTSUBTRACTNODE:
        case JJTMULNODE:
        case JJTDIVNODE:
            return;
            //return buildBinaryExpression(cqlNode.getType());

            // Boolean expression
        case JJTBOOLEAN_AND_NODE:
            builder.and();
            return;
            //return buildLogicFilter(JJTBOOLEAN_AND_NODE);

        case JJTBOOLEAN_OR_NODE:
            builder.or();
            return;
            //return buildLogicFilter(JJTBOOLEAN_OR_NODE);

        case JJTBOOLEAN_NOT_NODE:
            builder.not();
            return;
            //return buildLogicFilter(JJTBOOLEAN_NOT_NODE);

            // ----------------------------------------
            // between predicate actions
            // ----------------------------------------
        case JJTBETWEEN_NODE:
            return;
            //return this.builder.buildBetween();

        case JJTNOT_BETWEEN_NODE:
            return;
            //return this.builder.buildNotBetween();

            // ----------------------------------------
            // Compare predicate actions
            // ----------------------------------------
        case JJTCOMPARISONPREDICATE_EQ_NODE:
            builder.eq();
            return;
        case JJTCOMPARISONPREDICATE_GT_NODE:
            builder.gt();
            return;
        case JJTCOMPARISONPREDICATE_LT_NODE:
            builder.lt();
            return;
        case JJTCOMPARISONPREDICATE_GTE_NODE:
            builder.gte();
            return;
        case JJTCOMPARISONPREDICATE_LTE_NODE:
            builder.lte();
            return;
            //return buildBinaryComparasionOperator(cqlNode.getType());

        case JJTCOMPARISONPREDICATE_NOT_EQUAL_NODE:
            builder.neq();
            return;

            //Filter eq = buildBinaryComparasionOperator(JJTCOMPARISONPREDICATE_EQ_NODE);
            //Not notFilter = this.builder.buildNotFilter(eq);

            //return notFilter;

            // ----------------------------------------
            // Text predicate (Like)
            // ----------------------------------------
        case JJTLIKE_NODE:
            return;
            //return this.builder.buildLikeFilter(true);

        case JJTNOT_LIKE_NODE:
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
            return;
        case JJTROUTINEINVOCATION_GEOOP_DISJOINT_NODE:
            return;
        case JJTROUTINEINVOCATION_GEOOP_INTERSECT_NODE:
            builder.intersect(); return;
        case JJTROUTINEINVOCATION_GEOOP_TOUCH_NODE:
        case JJTROUTINEINVOCATION_GEOOP_CROSS_NODE:
        case JJTROUTINEINVOCATION_GEOOP_WITHIN_NODE:
        case JJTROUTINEINVOCATION_GEOOP_CONTAIN_NODE:
        case JJTROUTINEINVOCATION_GEOOP_OVERLAP_NODE:
            //return buildBinarySpatialOperator(cqlNode.getType());

        case JJTROUTINEINVOCATION_GEOOP_BBOX_NODE:
        case JJTROUTINEINVOCATION_GEOOP_BBOX_SRS_NODE:
            //return buildBBox(cqlNode.getType());

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
                builder.literal(new WKTReader().read(scan(n.getToken())));
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

    Comparison<Object> newComparison(Comparison.Type type) {
        Expression<Object> e1 = (Expression<Object>) stack.pop();
        Expression<Object> e2 = (Expression<Object>) stack.pop();
        return new Comparison<Object>(type, e2, e1);
    }

    String scan(final Token t) {

        Token end = t;
        
        while (end.next != null) {
            end = end.next;
        }

        return cql.substring(t.beginColumn -1, end.endColumn);

    }
}
