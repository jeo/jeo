package org.jeo.data;

import java.util.Iterator;

public interface Registry {

    Iterator<String> keys();

    Workspace get(String key);

    void dispose();
}
