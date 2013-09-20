package org.jeo.map;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jeo.filter.Expression;
import org.jeo.util.Convert;

/**
 * A styling rule. 
 * 
 * @author Justin Deoliveira, OpenGeo
 *
 */
public class Rule {

    /** 
     * ordered parts of the rule, composed of other rules, including self
     */
    List<Rule> parts = new ArrayList<Rule>();

    /**
     * style properties of the rule
     */
    Map<String,Object> props = null;

    /**
     * rule selectors
     */
    List<Selector> selectors = new ArrayList<Selector>();

    public Map<String, Object> properties() {
        if (props == null) {
            return Collections.emptyMap();
        }
        return props();
    }

    public List<Selector> getSelectors() {
        return selectors;
    }

    public List<Rule> nested() {
        List<Rule> nested = new ArrayList<Rule>();
        for (Rule r : parts) {
            if (r != this) {
                nested.add(r);
            }
        }
        return nested;
    }

    public Object get(String key) {
        Map<String,Object> props = props();
        return props.get(key);
    }

    public void put(String key, Object val) {
        props().put(key,  val);
    }

    public void putAll(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return;
        }

        props().putAll(map);
    }

    public void add(Rule rule) {
        parts.add(rule);
    }

    public <T> T eval(String key, Class<T> clazz) {
        return eval(null, key, clazz, null);
    }
    
    public <T> T eval(Object obj, String key, Class<T> clazz) {
        return eval(obj, key, clazz, null);
    }

    public <T> T eval(Object obj, String key, Class<T> clazz, T def) {
        Object result = evalRaw(obj, key, def);
        if (result != null) {
            return Convert.to(result, clazz, false).get(
                "Unable to convert " + result + " to " + clazz);
        }

        return null;
    }

    public <T> T[] evalArray(Object obj, String key, Class<T> clazz, T[] def) {
        Object result = evalRaw(obj, key, def);
        if (result == null) {
            return def;
        }
    
        if (result instanceof Collection) {
            result = ((Collection) result).toArray();
        }

        // check if the object is an array, and try to convert it to the right type
        if (result.getClass().isArray()) {
            //TODO: don't do conversion if the array is already of right type
            int len = Array.getLength(result);
            T[] arr = (T[]) Array.newInstance(clazz, len);

            for (int i = 0; i < len; i++) {
                Object o = Array.get(result, i);
                arr[i] = Convert.to(o, clazz).get("Unable to convert " + o + " to " + clazz);
            }

            return arr;
        }
    
        // parse as string delimited by spaces
        String[] split = result.toString().split(" ");
        T[] arr = (T[]) Array.newInstance(clazz, split.length);
        for (int i = 0; i < split.length; i++) {
            String s = split[i];
            arr[i] = Convert.to(s, clazz).get("Unable to convert " + s + " to " + clazz);
        }

        return arr;

        //TODO: attempt to convert from string delimiated by ' ', or ',' 
        //throw new IllegalArgumentException("Unable to convert " + obj + " to array");
    }

    Object evalRaw(Object obj, String key, Object def) {
        Map<String,Object> props = props();

        if (!props.containsKey(key)) {
            return def;
        }

        Object val = props.get(key);
        if (val == null) {
            return null;
        }

        Object result = val;
        if (val instanceof Expression) {
            result = ((Expression) val).evaluate(obj);
        }

        return result;
    }

    public RGB color(Object obj, String key, RGB def) {
        return eval(obj, key, RGB.class, def);
    }

    public String string(Object obj, String key, String def) {
        return eval(obj, key, String.class, def);
    }

    public Double number(Object obj, String key, Double def) {
        return eval(obj, key, Double.class, def);
    }
    
    public Float number(Object obj, String key, Float def) {
        return eval(obj, key, Float.class, def);
    }

    public Boolean bool(Object obj, String key, Boolean def) {
        return eval(obj, key, Boolean.class, def);
    }

    public Double[] numbers(Object obj, String key, Double... def) {
        return evalArray(obj, key, Double.class, def);
    }

    public Float[] numbers(Object obj, String key, Float... def) {
        return evalArray(obj, key, Float.class, def);
    }

    protected Map<String,Object> props() {
        if (props == null) {
            props = new LinkedHashMap<String, Object>();
            parts.add(this);
        }

        return props;
    }

//    protected double toDouble(Object obj) {
//        if (obj == null) {
//            return Double.NaN;
//        }
//    
//        if (obj instanceof Number) {
//            return ((Number)obj).doubleValue();
//        }
//    
//        return Double.parseDouble(obj.toString());
//    }
//
//    protected double[] toDoubles(String s, String delim) {
//        String[] split = s.split(delim);
//        double[] d = new double[split.length];
//        
//        for (int i = 0; i < d.length; i++) {
//            d[i] = toDouble(split[i].trim());
//        }
//        return d;
//    }
//    
//    protected RGB toRGB(Object obj) {
//        if (obj == null) {
//            return null;
//        }
//
//        if (obj instanceof RGB) {
//            return (RGB) obj;
//        }
//
//        return new RGB(obj.toString());
//    }

    /**
     * Flattens the rule by merging the top level rule with all nested rules.
     */
    public List<Rule> flatten() {
        //TODO: multiple levels of nesting?
        List<Rule> flat = new ArrayList<Rule>();

        if (parts.isEmpty()) {
            flat.add(this);
        }
        else {
            // intersect this with all the parts
            for (Rule r : parts) {
                if (r != this) {
                    r = merge(r);
                }
                flat.add(r);
            }
        }

        return flat;
    }

    /**
     * Merges this rule with another rule, resulting in a new rule object.
     * <p>
     * Any properties defined by this rule and <tt>other</tt> will be overwritten with the values
     * from <tt>other</tt>. 
     * </p>
     */
    public Rule merge(Rule other) {
        Rule merged = new Rule();

        //merge the properties
        merged.putAll(properties());
        merged.putAll(other.properties());

        //cross product all selectors
        List<Selector> selectors = new ArrayList<Selector>();
        for (Selector s1 : getSelectors()) {
            for (Selector s2 : other.getSelectors()) {
                selectors.add(s1.merge(s2));
            }
        }
        merged.getSelectors().addAll(selectors);

        return merged;
    }

    @Override
    public String toString() {
        return toString(0);
    }
    
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String pad = ""; for (int i = 0; i < indent; i++) { pad += " "; };
        
        sb.append(pad);
        for (Selector s : getSelectors()) {
            sb.append(s).append(",");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length()-1);
        }
        sb.append(pad).append(" {").append("\n");

        for (Map.Entry<String, Object> e : properties().entrySet()) {
            sb.append(pad).append("  ").append(e.getKey()).append(": ").append(e.getValue()).append(";\n");
        }

        for (Rule nested : nested()) {
            sb.append(nested.toString(indent+2)).append("\n");
        }

        sb.append(pad).append("}");
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((props == null) ? 0 : props.hashCode());

        for (Rule part : parts) {
            result = prime * result + ((part == null || part == this) ? 0 : part.hashCode());
        }

        result = prime * result + ((selectors == null) ? 0 : selectors.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Rule other = (Rule) obj;
        if (parts == null) {
            if (other.parts != null)
                return false;
        } else if (!parts.equals(other.parts))
            return false;
        if (props == null) {
            if (other.props != null)
                return false;
        } else if (!props.equals(other.props))
            return false;
        if (selectors == null) {
            if (other.selectors != null)
                return false;
        } else if (!selectors.equals(other.selectors))
            return false;
        return true;
    }

    
}
