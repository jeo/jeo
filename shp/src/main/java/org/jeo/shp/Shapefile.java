package org.jeo.shp;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.jeo.data.FileDriver;

public class Shapefile extends FileDriver<ShpDataset> {

    public static ShpDataset open(File file) throws IOException {
        return new Shapefile().open(file, null);
    }

    @Override
    public String getName() {
        return "Shapefile";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("shp");
    }

    @Override
    public Class<ShpDataset> getType() {
        return ShpDataset.class;
    }

    @Override
    public boolean canOpen(File file, Map<?,Object> opts) {
        return super.canOpen(file, opts) && file.isFile() 
            && file.getName().toLowerCase().endsWith(".shp");
    }

    @Override
    public ShpDataset open(File file, Map<?, Object> opts) throws IOException {
        return new ShpDataset(file); 
    }
}
