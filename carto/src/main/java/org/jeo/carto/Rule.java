package org.jeo.carto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jeo.feature.Feature;
import org.jeo.filter.Filter;

public class Rule {

    String name;
    Filter<Feature> filter;

    Map<String,Object> props = new LinkedHashMap<String, Object>();
    List<Rule> nested = new ArrayList<Rule>();

    public Rule() {
    }

    public Rule(String name) {
        this.name = name;
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

    public Object get(String key) {
        return props.get(key);
    }

    public void put(String key, Object val) {
        props.put(key,  val);
    }

    public List<Rule> getNested() {
        return nested;
    }
}
