package org.jeo.map;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
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
        return get(key, null);
    }
    
    public Object get(String key, Object def) {
        if (props == null) {
            return def;
        }

        Map<String,Object> props = props();
        return props.containsKey(key) ? props.get(key) : def;
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

    protected Map<String,Object> props() {
        if (props == null) {
            props = new LinkedHashMap<String, Object>();
            parts.add(this);
        }

        return props;
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

    /**
     * Flattens the rule by merging the top level rule with all nested rules.
     */
    public List<Rule> flatten() {
        //TODO: multiple levels of nesting?
        List<Rule> flat = new ArrayList<Rule>();

        for (Rule r : parts) {
            if (r != this) {
                r = merge(r);
            }
            flat.add(r);
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
        sb.append(pad).append("{").append("\n");

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
        result = prime * result + ((parts == null) ? 0 : parts.hashCode());
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
