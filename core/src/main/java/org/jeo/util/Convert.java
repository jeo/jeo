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
package org.jeo.util;

import org.jeo.map.RGB;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
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

    public static <T> Optional<T> to(Object obj, Class<T> clazz) {
        return to(obj, clazz, true);
    }
    
    public static <T> Optional<T> to(Object obj, Class<T> clazz, boolean safe) {
        if (clazz.isInstance(obj)) {
            return Optional.of((T) obj);
        }

        if (clazz == String.class) {
            return (Optional<T>) toString(obj);
        }
        else if (Number.class.isAssignableFrom(clazz)) {
            return toNumber(obj, (Class)clazz);
        }
        else if (Boolean.class.isAssignableFrom(clazz)) {
            return (Optional<T>) toBoolean(obj);
        }
        else if (File.class.isAssignableFrom(clazz)) {
            return (Optional<T>) toFile(obj);
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
            return Optional.of(converted);
        }

        return Optional.nil(clazz);
    }

    public static Optional<String> toString(Object obj) {
        if (obj != null) {
            return Optional.of(obj.toString());
        }
        return Optional.nil(String.class);
    }

    public static Optional<Boolean> toBoolean(Object obj) {
        if (obj instanceof Boolean) {
            return Optional.of((Boolean) obj);
        }

        if (obj instanceof String) {
            return Optional.of(Boolean.parseBoolean((String)obj));
        }

        return null;
    }

    public static Optional<File> toFile(Object obj) {
        if (obj instanceof File) {
            return Optional.of((File) obj);
        }

        if (obj instanceof String) {
            return Optional.of(new File((String)obj));
        }

        return null;
    }

    public static <T extends Number> Optional<T> toNumber(Object obj, Class<T> clazz) {
        Optional<Number> n = toNumber(obj);
        if (!n.has()) {
            return Optional.nil(clazz);
        }

        if (clazz == Byte.class) {
            return Optional.of(clazz.cast(new Byte(n.get().byteValue())));
        }
        if (clazz == Short.class) {
            return Optional.of(clazz.cast(new Short(n.get().shortValue())));
        }
        if (clazz == Integer.class) {
            return Optional.of(clazz.cast(new Integer(n.get().intValue())));
        }
        if (clazz == Long.class) {
            return Optional.of(clazz.cast(new Long(n.get().longValue())));
        }
        if (clazz == Float.class) {
            return Optional.of(clazz.cast(new Float(n.get().floatValue())));
        }
        if (clazz == Double.class) {
            return Optional.of(clazz.cast(new Double(n.get().doubleValue())));
        }
        
        return Optional.nil(clazz);
    }

    public static <T extends Number> Optional<List<T>> toNumbers(Object obj, Class<T> clazz) {
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
                Optional<T> num = toNumber(o, clazz);
                if (!num.has()) {
                    return (Optional) Optional.nil(List.class);
                }
                converted.add(num.get());
            }
            return Optional.of(converted);
        }

        return (Optional) Optional.nil(List.class);
    }

    public static Optional<Number> toNumber(Object obj) {
        if (obj instanceof Number) {
            return Optional.of((Number) obj);
        }
        if (obj instanceof String) {
            String str = (String) obj;
            try {
                return Optional.of((Number)Long.parseLong(str));
            }
            catch(NumberFormatException e) {
                try {
                    return Optional.of((Number)Double.parseDouble(str));
                }
                catch(NumberFormatException e1) {
                }
            }
        }
        return Optional.nil(Number.class);
    }

    public static Optional<RGB> toColor(Object obj) {
        if (obj == null) {
            return Optional.nil(RGB.class);
        }

        if (obj instanceof RGB) {
            return Optional.of((RGB)obj);
        }

        return Optional.of(new RGB(obj.toString()));
    }

    public static Optional<Reader> toReader(Object obj) throws IOException {
        if (obj instanceof Reader) {
            return Optional.of((Reader) obj);
        }

        if (obj instanceof InputStream) {
            return Optional.of((Reader)new BufferedReader(new InputStreamReader((InputStream)obj)));
        }

        if (obj instanceof File) {
            return Optional.of((Reader)new BufferedReader(new FileReader((File)obj)));
        }
        if (obj instanceof String) {
            return Optional.of((Reader) new StringReader((String) obj));
        }

        return null;
    }
}
