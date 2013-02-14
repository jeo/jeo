package org.jeo.data;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public abstract class FileWorkspaceFactory<T extends Workspace> implements WorkspaceFactory<T> {

    public static final String FILE = "file";

    List<String> exts;

    protected FileWorkspaceFactory(String... exts) {
        this.exts = Arrays.asList(exts);
    }

    @Override
    public T create(Map<String, Object> map) throws IOException {
        if (map.containsKey(FILE)) {
            File file = toFile(map.get(FILE));
            if (file != null) {
                String fn = file.getName();
                int dot = fn.lastIndexOf('.');
                if (exts.contains(fn.substring(dot+1))) {
                    return createFromFile(file);    
                }
            }
        }
        return null;
    }

    protected File toFile(Object object) {
        if (object instanceof File) {
            return (File) object;
        }

        if (object instanceof String) {
            return new File((String)object);
        }

        return null;
    }

    protected abstract T createFromFile(File file) throws IOException;
}
