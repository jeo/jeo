package org.jeo.feature;

import java.util.HashMap;
import java.util.Map;

/**
 * Feature wrapper that tracks changes to an underlying Feature object.
 *
 * @author Justin Deoliveira, OpenGeo
 */
public class DiffFeature extends FeatureWrapper {

    Map<String,Object> changed;

    public DiffFeature(Feature feature) {
        super(feature);
        changed = new HashMap<String, Object>();
    }

    /**
     * The diff map.
     */
    public Map<String, Object> getChanged() {
        return changed;
    }

    /**
     * Applies the changes made to the underlying feature.
     */
    public void apply() {
        for (Map.Entry<String, Object> e: changed.entrySet()) {
            delegate.put(e.getKey(), e.getValue());
        }
        changed.clear();
    }

    public Object get(String key) {
        if (changed.containsKey(key)) {
            return changed.get(key);
        }

        return delegate.get(key);
    }

    public void put(String key, Object val) {
        changed.put(key, val);
    }
}
