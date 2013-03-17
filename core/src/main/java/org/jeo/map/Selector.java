package org.jeo.map;

import java.util.ArrayList;
import java.util.List;

import org.jeo.filter.Filter;

public class Selector {

    String id;
    String name;
    String attachment;
    boolean wildcard = false;
    List<String> classes = new ArrayList<String>();
    Filter filter;
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getAttachment() {
        return attachment;
    }
    
    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }

    public boolean isWildcard() {
        return wildcard;
    }

    public void setWildcard(boolean wildcard) {
        this.wildcard = wildcard;
    }

    public List<String> getClasses() {
        return classes;
    }
    
    public Filter getFilter() {
        return filter;
    }
    
    public void setFilter(Filter filter) {
        this.filter = filter;
    }
    
    public Selector merge(Selector other) {
        String id = null;
        try {
            id = merge(getId(), other.getId());
        }
        catch(IllegalArgumentException e) {
            throw new IllegalArgumentException("Unable to merge id selectors");
        }
    
        String name = null;
        try {
            name = merge(getName(), other.getName());
        }
        catch(IllegalArgumentException e) {
            throw new IllegalArgumentException("Unable to merge type selectors");
        }
    
        String attachment = null;
        try {
            attachment = merge(getAttachment(), other.getAttachment());
        }
        catch(IllegalArgumentException e) {
            throw new IllegalArgumentException("Unable to merge two selectors with an attachment");
        }
    
        Selector merged = new Selector();
        merged.setId(id);
        merged.setName(name);
        merged.setAttachment(attachment);
        merged.getClasses().addAll(getClasses());
        merged.getClasses().addAll(other.getClasses());
    
        Filter filter = getFilter() != null ? getFilter() : Filter.TRUE;
        if (other.getFilter() != null) {
            filter = filter.and(other.getFilter());
        }
        merged.setFilter(filter);
    
        return merged;
    }
    
    String merge(String s1, String s2) {
        if (s1 == s2) {
            return s1;
        }
    
        if (s1 == null && s2 != null) {
            return s2;
        }
    
        if (s2 == null && s1 != null) {
            return s1;
        }
    
        if (s1.equals(s2)) {
            return s1;
        }
        
        throw new IllegalArgumentException();
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();

        if (name != null) {
            sb.append(name);
        }
        if (id != null) {
            sb.append("#").append(id);
        }
        for (String c : classes) {
            sb.append(".").append(c);
        }
        if (filter != null && filter != Filter.TRUE) {
            sb.append("[").append(filter).append("]");
        }
        if (attachment != null) {
            sb.append("::").append(attachment);
        }

        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((attachment == null) ? 0 : attachment.hashCode());
        result = prime * result + ((classes == null) ? 0 : classes.hashCode());
        result = prime * result + ((filter == null) ? 0 : filter.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        Selector other = (Selector) obj;
        if (attachment == null) {
            if (other.attachment != null)
                return false;
        } else if (!attachment.equals(other.attachment))
            return false;
        if (classes == null) {
            if (other.classes != null)
                return false;
        } else if (!classes.equals(other.classes))
            return false;
        if (filter == null) {
            if (other.filter != null)
                return false;
        } else if (!filter.equals(other.filter))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    
}
