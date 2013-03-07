package org.jeo.map;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jeo.feature.Feature;
import org.jeo.filter.Filter;

/**
 * A styling rule. 
 * 
 * @author Justin Deoliveira, OpenGeo
 *
 */
public class Rule {

    public static enum Type {
        NAME, CLASS, ANONYMOUS;
    }

    String name;
    Type type;

    Filter<Feature> filter = (Filter<Feature>) Filter.TRUE;

    Map<String,Object> props= new LinkedHashMap<String, Object>();
    List<Rule> nested = new ArrayList<Rule>();

    public Rule() {
        this(null, Type.ANONYMOUS);
    }

    public Rule(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Filter<Feature> getFilter() {
        return filter;
    }

    public void setFilter(Filter<Feature> filter) {
        this.filter = filter;
    }

    public Map<String, Object> getProperties() {
        return props;
    }

    public Object get(String key) {
        return get(key, null);
    }

    public Object get(String key, Object def) {
        return props.containsKey(key) ? props.get(key) : def;
    }

    public void put(String key, Object val) {
        props.put(key,  val);
    }

    public String string(String key, String def) {
        Object obj = get(key, def);
        return obj != null ? obj.toString() : null;
    }

    public double number(String key, double def) {
        return doub(get(key, def));
    }

    public float number(String key, float def) {
        return (float) number(key, (double) def);
    }

    public double[] numbers(String key, double... def) {
        Object obj = get(key, def);
        if (obj == null) {
            return null;
        }

        if (obj instanceof double[]) {
            return (double[]) obj;
        }

        if (obj.getClass().isArray()) {
            int n = Array.getLength(obj);
            double[] d = new double[n];
            for (int i = 0; i < n; i++) {
                d[i] = doub(Array.get(obj, i));
            }

            return d;
        }

        //TODO: attempt to convert from string delimiated by ' ', or ',' 
        throw new IllegalArgumentException("Unable to convert " + obj + " to array");
    }

    double doub(Object obj) {
        if (obj == null) {
            return Double.NaN;
        }

        if (obj instanceof Number) {
            return ((Number)obj).doubleValue();
        }

        return Double.parseDouble(obj.toString());
    }
    
    public List<Rule> getNested() {
        return nested;
    }

    public void merge(Rule other) {
        props.putAll(other.getProperties());
    }

    public static void main(String[] args) {
        //double[] x = new double[]{};
        Object x = new Double[]{};

        //System.out.println(x instanceof double[]);
        System.out.println(x instanceof Double[]);
        System.out.println(x.getClass().isArray());
    }

}
