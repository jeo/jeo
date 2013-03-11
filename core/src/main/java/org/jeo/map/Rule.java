package org.jeo.map;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A styling rule. 
 * 
 * @author Justin Deoliveira, OpenGeo
 *
 */
public class Rule {

    /**
     * style properties of the rule
     */
    Map<String,Object> props = new LinkedHashMap<String, Object>();

    /**
     * rule selectors
     */
    List<Selector> selectors = new ArrayList<Selector>();

    /**
     * nested rules
     */
    List<Rule> nested = new ArrayList<Rule>();

    public Map<String, Object> getProperties() {
        return props;
    }

    public List<Selector> getSelectors() {
        return selectors;
    }

    public List<Rule> getNested() {
        return nested;
    }

    public void merge(Rule other) {
        props.putAll(other.getProperties());
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

    public RGB color(String key, RGB def) {
        Object obj = get(key, def);
        return obj != null ? toRGB(obj) : null;
    }

    public String string(String key, String def) {
        Object obj = get(key, def);
        return obj != null ? obj.toString() : null;
    }

    public double number(String key, double def) {
        return toDouble(get(key, def));
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
                d[i] = toDouble(Array.get(obj, i));
            }
    
            return d;
        }
    
        //TODO: attempt to convert from string delimiated by ' ', or ',' 
        throw new IllegalArgumentException("Unable to convert " + obj + " to array");
    }

    protected double toDouble(Object obj) {
        if (obj == null) {
            return Double.NaN;
        }
    
        if (obj instanceof Number) {
            return ((Number)obj).doubleValue();
        }
    
        return Double.parseDouble(obj.toString());
    }

    protected RGB toRGB(Object obj) {
        if (obj == null) {
            return null;
        }

        if (obj instanceof RGB) {
            return (RGB) obj;
        }

        return new RGB(obj.toString());
    }

}
