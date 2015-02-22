/* Copyright 2013 The jeo project. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jeo.filter;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;

import org.jeo.vector.Feature;
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

    public String property() {
        return property;
    }

    /**
     * Check to see if the provided object has the property. The return
     * value of evaluate can be ambiguous (is there a property with a null value
     * or just no property) so this method can be used if needed.
     *
     * @param obj the object to evaluate
     * @return true if the property exists and false otherwise but does not check the value
     */
    public boolean has(Object obj) {
        boolean has;
        if (obj instanceof Feature) {
            has = ((Feature) obj).has(property);
        } else {
            has = resolveMethod(obj, property) != null;
        }
        return has;
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

    protected Method resolveMethod(Object target, String prop) {
        Class<?> clazz = target.getClass();
        Method found = null;
        for (String name :
            Arrays.asList(prop, "get"+Character.toUpperCase(prop.charAt(0))+prop.substring(1))) {
            try {
                Method m = clazz.getMethod(name);
                if (m != null && m.getReturnType() != null) {
                    found = m;
                    break;
                }
            } catch (NoSuchMethodException ex) {
                // try again
            } catch (SecurityException ex) {
                // dang
            }
        }
        return found;
    }

    protected Object get(Object target, String prop) {
        Object result = null;
        Method method = resolveMethod(target, prop);
        if (method != null) {
            try {
                result = method.invoke(target);
            } catch (Exception e) {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Error invoking method: "+ method.getName() +" of class " + target.getClass().getName(), e);
                }
            }
        }
        return result;
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
