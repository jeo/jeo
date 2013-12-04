package org.jeo.filter;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;

import org.jeo.feature.Feature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Evaluates a field/property of a {@link Feature} object.
 *  
 * @author Justin Deoliveira, OpenGeo
 */
public class Property implements Expression {

    static Logger LOG = LoggerFactory.getLogger(Property.class);

    String property;

    public Property(String property) {
        this.property = property;
    }

    public String getProperty() {
        return property;
    }

    @Override
    public Object evaluate(Object obj) {
        return resolve(obj);
    }

    protected Object resolve(Object obj) {
        if (obj == null) {
            return null;
        }

        // first check for feature
        if (obj instanceof Feature) {
            //TODO: nested properties for features?
            return ((Feature)obj).get(property);
        }

        // fall back onto reflection
        Iterator<String> parts = Arrays.asList(property.split("\\.")).iterator();
        Object target = obj;
        
        while(parts.hasNext() && target != null) {
            target = get(target, parts.next());
        }

        return target;
    }

    protected Object get(Object target, String prop) {
        Class<?> clazz = target.getClass();
        for (String name : 
            Arrays.asList(prop, "get"+Character.toUpperCase(prop.charAt(0))+prop.substring(1))) {

            try {
                Method m = clazz.getMethod(name);
                if (m != null && m.getReturnType() != null) {
                    return m.invoke(target);
                }
            } catch (Exception e) {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Error invoking method: "+name+" of class " + clazz.getName(), e);
                }
            } 
        }
        return null;
    }

    @Override
    public Object accept(FilterVisitor visitor, Object obj) {
        return visitor.visit(this, obj);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((property == null) ? 0 : property.hashCode());
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
        Property other = (Property) obj;
        if (property == null) {
            if (other.property != null)
                return false;
        } else if (!property.equals(other.property))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "[" + property + "]";
    }
}
