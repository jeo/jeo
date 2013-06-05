package org.jeo.mongo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Path {

    /** serialVersionUID */
    private static final long serialVersionUID = 1L;

    List<String> parts;
    String join;

    public Path() {
        this(new ArrayList<String>());
    }

    public Path(String path) {
        this(Arrays.asList(path.split("\\.")));
    }

    public Path(List<String> path) {
        parts = new ArrayList<String>(path);
        
        StringBuilder sb = new StringBuilder();
        for (String str: parts) {
            sb.append(str).append(".");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length()-1);
        }
        join = sb.toString();
    }

    public List<String> getParts() {
        return parts;
    }

    public Path prepend(String part) {
        Path p = new Path(parts);
        p.parts.add(0, part);
        return p;
    }

    public Path append(String part) {
        Path p = new Path(parts);
        p.parts.addAll(new Path(part).getParts());
        return p;
    }

    public String join() {
        return join;
    }

    @Override
    public String toString() {
        return join;
    }
}
