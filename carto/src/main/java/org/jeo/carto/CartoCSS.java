package org.jeo.carto;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.jeo.data.FileDriver;
import org.jeo.map.Style;

public class CartoCSS extends FileDriver<Style> {

    @Override
    public String getName() {
        return "CartoCSS";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("carto", "css");
    }

    @Override
    public Class<Style> getType() {
        return Style.class;
    }

    @Override
    protected Style open(File file, Map<?, Object> opts) throws IOException {
        return Carto.parse(file);
    }

}
