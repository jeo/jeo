package org.jeo.feature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.vividsolutions.jts.geom.Geometry;

public class Schema implements Iterable<Field> {

    String name;
    List<Field> fields;

    public static Schema build(String name) {
        return build(name, Geometry.class);
    }

    public static Schema build(String name, Object... fields) {
        if (fields.length % 2 != 0) {
            throw new IllegalArgumentException("fields must be specified as String,Class pairs");
        }

        List<Field> flds = new ArrayList<Field>();
        for (int i = 0; i < fields.length; i += 2) {
            flds.add(new Field((String) fields[i], (Class<?>)fields[i+1]));
        }

        return new Schema(name, flds);
    }

    public Schema(String name, List<Field> fields) {
        this.name = name;
        this.fields = Collections.unmodifiableList(fields);
    }

    public String getName() {
        return name;
    }

    public Field geometry() {
        for (Field f : this) {
            if (Geometry.class.isAssignableFrom(f.getType())) {
                return f;
            }
        }
        return null;
    }

    public List<Field> getFields() {
        return fields;
    }

    public Field field(String name) {
        int i = indexOf(name);
        return i != -1 ? fields.get(i) : null;
    }

    public int indexOf(String name) {
        //TODO: potentially add an index of name to field
        for (int i = 0; i < fields.size(); i++) {
            Field f = fields.get(i);
            if (f.getName().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    public int size() {
        return fields.size();
    }

    @Override
    public Iterator<Field> iterator() {
        return fields.iterator();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(name).append("[");
        if (!fields.isEmpty()) {
            for (Field f : fields) {
                sb.append(f).append(",");
            }
            sb.setLength(sb.length()-1);
        }
        return sb.toString();
    }
}
