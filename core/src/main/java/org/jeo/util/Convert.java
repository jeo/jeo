package org.jeo.util;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Conversion utility class.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class Convert {

    public static <T> T to(Object obj, Class<T> clazz) {
        return to(obj, clazz, true);
    }
    
    public static <T> T to(Object obj, Class<T> clazz, boolean safe) {
        if (clazz.isInstance(obj)) {
            return (T) obj;
        }

        if (clazz == String.class) {
            return clazz.cast(toString(obj));
        }
        else if (Number.class.isAssignableFrom(clazz)) {
            return clazz.cast(toNumber(obj, (Class)clazz));
        }
        else if (Boolean.class.isAssignableFrom(clazz)) {
            return clazz.cast(toBoolean(obj));
        }
        else if (File.class.isAssignableFrom(clazz)) {
            return clazz.cast(toFile(obj));
        }
        else if (!safe && obj != null) {
            //constructor trick
            T converted = null;
            try {
                try {
                    converted = clazz.getConstructor(obj.getClass()).newInstance(obj);
                } 
                catch(NoSuchMethodException e) {
                    //try with string
                    converted = clazz.getConstructor(String.class).newInstance(obj.toString());
                }
            }
            catch(Exception e) {
            }
            return converted;
        }
        return null;
    }

    public static String toString(Object obj) {
        if (obj != null) {
            return obj.toString();
        }
        return null;
    }

    public static Boolean toBoolean(Object obj) {
        if (obj instanceof Boolean) {
            return (Boolean) obj;
        }

        if (obj instanceof String) {
            return Boolean.parseBoolean((String)obj);
        }

        return null;
    }

    public static File toFile(Object obj) {
        if (obj instanceof File) {
            return (File) obj;
        }

        if (obj instanceof String) {
            return new File((String)obj);
        }

        return null;
    }

    public static <T extends Number> T toNumber(Object obj, Class<T> clazz) {
        Number n = toNumber(obj);
        if (n == null) {
            return null;
        }

        if (clazz == Byte.class) {
            return clazz.cast(new Byte(n.byteValue()));
        }
        if (clazz == Short.class) {
            return clazz.cast(new Short(n.shortValue()));
        }
        if (clazz == Integer.class) {
            return clazz.cast(new Integer(n.intValue()));
        }
        if (clazz == Long.class) {
            return clazz.cast(new Long(n.longValue()));
        }
        if (clazz == Float.class) {
            return clazz.cast(new Float(n.floatValue()));
        }
        if (clazz == Double.class) {
            return clazz.cast(new Double(n.doubleValue()));
        }
        return null;
    }

    public static <T extends Number> List<T> toNumbers(Object obj, Class<T> clazz) {
        Collection<Object> l = null;
        if (obj instanceof Collection) {
            l = (Collection<Object>) obj;
        }
        else if (obj.getClass().isArray()) {
            l = new ArrayList<Object>();
            for (int i = 0; i < Array.getLength(obj); i++) {
                l.add(Array.get(obj, i));
            }
        }
        else if (obj instanceof String) {
            l = (List) Arrays.asList(obj.toString().split(" "));
        }

        if (l != null) {
            List<T> converted = new ArrayList<T>();
            for (Object o : l) {
                T num = toNumber(o, clazz);
                if (num == null) {
                    return null;
                }
                converted.add(num);
            }
            return converted;
        }

        return null;
    }

    public static Number toNumber(Object obj) {
        if (obj instanceof Number) {
            return (Number) obj;
        }
        if (obj instanceof String) {
            String str = (String) obj;
            try {
                return Long.parseLong(str);
            }
            catch(NumberFormatException e) {
                try {
                    return Double.parseDouble(str);
                }
                catch(NumberFormatException e1) {
                }
            }
        }
        return null;
    }
}
