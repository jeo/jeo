package org.jeo.data;

import java.io.IOException;
import java.util.Map;

public interface WorkspaceFactory<T extends Workspace> {

    T create(Map<String,Object> map) throws IOException;
}
