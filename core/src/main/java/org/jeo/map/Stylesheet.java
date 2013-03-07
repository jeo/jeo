package org.jeo.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jeo.feature.Feature;
import org.jeo.map.Rule.Type;

/**
 * Defines the rules used to symbolize a map.
 * <p>
 * 
 * </p> 
 * @author Justin Deoliveira, OpenGeo
 */
public class Stylesheet {

    Map<String,Object> props = new HashMap<String, Object>();
    List<Rule> rules = new ArrayList<Rule>();

    public Map<String,Object> getProperties() {
        return props;
    }

    public Object get(String key) {
        return get(key, null);
    }

    /**
     * Returns a property of the stylesheet.
     *  
     * @param key The property key. 
     * @param def The default value in case the property has no value.
     * 
     * @return The property value, or <tt>def</tt>.
     */
    public Object get(String key, Object def) {
        return props.containsKey(key) ? props.get(key) : def;
    }

    /**
     * Sets a property of the stylesheet.
     * 
     * @param key The property key.
     * @param value The property value.
     */
    public void put(String key, Object value) {
        props.put(key, value);
    }

    /**
     * The rules making up the stylesheet.
     */
    public List<Rule> getRules() {
        return rules;
    }

    
    public Rule apply(Layer l, Feature f) {
        Rule result = new Rule();

        for (Rule r : rules) {
            boolean match = false;
            if (r.getType() == Type.NAME && r.getName().equals(l.getName())) {
                match = true;
            }

            if (!match && r.getFilter().apply(f)) {
                match = true;
            }

            if (match) {
                result.merge(r);
            }

            //TODO: nested rules
        }

        return result;
    }
}
