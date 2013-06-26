package org.jeo.sql;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jeo.filter.All;
import org.jeo.filter.Comparison;
import org.jeo.filter.Expression;
import org.jeo.filter.Filter;
import org.jeo.filter.FilterVisitor;
import org.jeo.filter.Function;
import org.jeo.filter.Id;
import org.jeo.filter.Literal;
import org.jeo.filter.Logic;
import org.jeo.filter.None;
import org.jeo.filter.Property;
import org.jeo.filter.Spatial;
import org.jeo.util.Pair;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Transforms a filter object into SQL.
 * <p>
 * This base implementation encodes using "standard" SQL and SFS (simple features for SQL)
 * conventions. Format implementations should subclass and override methods as need be.
 * </p>  
 * <p>
 * The encoder operates in two modes in one of two modes determined by {@link #isPrepared()}. When
 * <code>true</code> the encoder will emit prepared statement sql. Arguments for the prepared 
 * statement are stored in {@link #getArgs()}. When <code>false</code> the encoder will encode 
 * literals directly. 
 * </p>
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class FilterSQLEncoder extends FilterVisitor {

    protected PrimaryKey pkey;

    protected DbTypes dbtypes;

    protected SQL sql;

    protected boolean prepared = true;
    protected List<Pair<Object, Integer>> args;

    public FilterSQLEncoder(PrimaryKey pkey, DbTypes dbtypes) {
        this.pkey = pkey;
        this.dbtypes = dbtypes;

        sql = new SQL();
        args = new ArrayList<Pair<Object, Integer>>();
    }

    public SQL getSQL() {
        return sql;
    }

    public List<Pair<Object, Integer>> getArgs() {
        return args;
    }

    public boolean isPrepared() {
        return prepared;
    }

    public void setPrepared(boolean prepared) {
        this.prepared = prepared;
    }

    public String encode(Filter filter, Object obj) {
        sql.clear();
        args.clear();

        filter.accept(this, obj);
        return sql.toString();
    }

    protected void abort(Object obj, String reason) {
        throw new FilterSQLException(
            String.format("Unable to encode %s as sql, %s @ %s", obj, reason, sql.toString())); 
    }

    public Object visit(Literal literal, Object obj) {
        Object val = literal.evaluate(null);

        if (val == null) {
            if (prepared) {
                sql.add("?");
                args.add(new Pair<Object, Integer>(null, dbtypes.toSQL(Geometry.class)));
            }
            else {
                sql.add("NULL");
            }
        }
        else {
            if (val instanceof Geometry) {
                encode((Geometry)val, obj);
            }
            else {
                if (prepared) {
                    sql.add("?");
                    args.add(new Pair<Object, Integer>(val, 
                        val != null ? dbtypes.toSQL(val.getClass()) : Types.NULL));
                }
                else {
                    if (val instanceof Number) {
                        sql.add(val);
                    }
                    else if (val instanceof Date) {
                        sql.add(encode((Date)val, obj));
                    }
                    else {
                        sql.str(val.toString());
                    }
                }
            }
        }

        return obj;
    }

    protected void encode(Geometry geo, Object obj) {
        if (prepared) {
            sql.add("ST_GeomFromText(?,?)");
            args.add(new Pair<Object,Integer>(geo.toText(), Types.VARCHAR));
            args.add(new Pair<Object,Integer>(geo.getSRID(), Types.INTEGER));
        }
        else {
            sql.add("ST_GeomFromText(").str(geo.toText()).add(",").add(geo.getSRID()).add(")");
        }
    }

    protected String encode(Date date, Object obj) {
        abort(date, "not implemented");
        return null;
    }

    public Object visit(Property property, Object obj) {
        sql.name(property.getProperty());
        return obj;
    }

    public Object visit(Function function, Object obj) {
        sql.add(function.getName()).add("(");
        for (Expression e : function.getArgs()) {
            e.accept(this, obj);
        }
        sql.add(")");
        return obj;
    }

    public final Object visit(All all, Object obj) {
        sql.add("1 = 1");
        return obj;
    }

    public Object visit(None none, Object obj) {
        sql.add("1 = 0");
        return obj;
    }

    public Object visit(Id id, Object obj) {
        if (pkey == null) {
            abort(id, "Id filter requires primary key");
        }
        if (pkey.getColumns().size() != 1) {
            abort(id, "Id filter only supported for single column primary key");
        }

        PrimaryKeyColumn pkeyCol = pkey.getColumns().get(0);
        sql.name(pkeyCol.getName()).add(" IN (");
        for (Expression e : id.getIds()) {
            e.accept(this, obj);
            sql.add(",");
        }
        sql.trim(1).add(")");
        return obj;
    }

    public Object visit(Logic logic, Object obj) {
        String op = logic.getType().name();
        for (Filter f : logic.getParts()) {
            sql.add("(");
            f.accept(this, obj);
            sql.add(") ").add(op).add(" ");
        }
        sql.trim(op.length()+2);
        return obj;
    }

    public Object visit(Comparison compare, Object obj) {
        compare.getLeft().accept(this, obj);
        sql.add(" ").add(compare.getType().toString()).add(" ");
        compare.getRight().accept(this,  obj);
        return obj;
    }

    public Object visit(Spatial spatial, Object obj) {
        String function = null;
        switch(spatial.getType()) {
        case INTERSECT:
            function = "ST_Intersects";
            break;
        case COVER:
            function = "ST_Covers";
            break;
        case CROSS:
            function = "ST_Crosses";
            break;
        case DISJOINT:
            function = "ST_Disjoint";
            break;
        case OVERLAP:
            function = "ST_Overlaps";
            break;
        case TOUCH:
            function = "ST_Touches";
            break;
        case WITHIN:
            function = "ST_Within";
            break;
        default:
            abort(spatial, "unsupported spatial filter");
        }

        sql.add(function).add("(");
        spatial.getLeft().accept(this, obj);
        sql.add(", ");
        spatial.getRight().accept(this, obj);
        sql.add(")");
        return obj;
    }
}
