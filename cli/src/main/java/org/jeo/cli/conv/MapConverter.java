package org.jeo.cli.conv;

import java.util.LinkedHashMap;
import java.util.Map;

import org.jeo.data.FileDriver;

import com.beust.jcommander.IStringConverter;

public class MapConverter implements IStringConverter<Map> {

    @Override
    public Map convert(String str) {
        Map<String,String> map = new LinkedHashMap<String, String>();

        for (String kv : str.split(" *; *")) {
            String[] split = kv.split(" *: *");
            if (split.length == 1) {
                //assume file
                map.put(FileDriver.FILE.getName(), split[0]);
            }
            else {
                map.put(split[0], split[1]);    
            }
        }

        return map;
    }

}
