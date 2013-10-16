package org.jeo.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jeo.geojson.simple.JSONObject;
import org.jeo.geojson.simple.JSONValue;
import org.jeo.util.Convert;
import org.jeo.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registry defined by a JSON file.
 * <p>
 * The registry contains a JSON object whose keys are names of the items in the registry. Each key 
 * maps to an object that contains the following properties.
 * <ul>
 *   <li>driver - The name or alias identifying the driver for the item.
 *   <li>keys - Object containing the key / values pairs defining the connection options.   
 * </ul>
 * The following is an example consisting of a single GeoJSON dataset.
 * <code><pre>
 * {
 *   "states": {
 *      "driver": "GeoJSON", 
 *      "keys": {
 *        "file": "states.json"
 *      }
 *   }
 * }
 * </pre></code>
 * </p>
 * @author Justin Deoliveira, Boundless
 *
 */
public class JSONRegistry implements Registry {

    static Logger LOG = LoggerFactory.getLogger(JSONRegistry.class);

    JSONObject reg;
    File regFile;

    public JSONRegistry(JSONObject reg) {
        this.reg = reg;
    }

    public JSONRegistry(File file) {
        this.regFile = file;
    }
    
    @Override
    public Iterable<DataRef<?>> list() throws IOException {
        JSONObject reg = reg();

        List<DataRef<?>> list = new ArrayList<DataRef<?>>();
        
        for (Object k : reg.keySet()) {
            String key = k.toString();

            Object o = reg.get(key);
            if (!(o instanceof JSONObject)) {
                LOG.debug(key + " is not an object");
                return null;
            }

            JSONObject obj = (JSONObject) o;
            if (!obj.containsKey("driver")) {
                LOG.debug(key + " does not define a 'driver' key");
                return null;
            }

            String driver = obj.get("driver").toString();
            Driver<?> drv = Drivers.find(driver);
            if (drv == null) {
                LOG.debug("unable to load driver: " + driver);
                return null;
            }

            list.add(new DataRef(key, drv));
        }
        return list;
    }

    @Override
    public Object get(String name) throws IOException {
        JSONObject reg = reg();

        JSONObject obj = (JSONObject) reg.get(name);
        if (obj == null) {
            return null;
        }

        if (!obj.containsKey("driver")) {
            throw new IOException(name + " does not define a 'driver' key");
        }

        String driver = obj.get("driver").toString();
        Driver<?> drv = Drivers.find(driver);
        if (drv == null) {
            throw new IOException("unable to load driver: " + driver);
        }

        Map<Object,Object> opts = new HashMap<Object,Object>();
        if (obj.containsKey("keys")) {
            opts.putAll((Map)obj.get("keys"));
        }
        else if (obj.containsKey("file")){
            opts.put("file", obj.get("file"));
        }

        // look for any file keys, make relative paths relative to the registry file
        if (regFile != null) {
            for (Map.Entry<Object, Object> kv : opts.entrySet()) {
                if ("file".equalsIgnoreCase(kv.getKey().toString())) {
                    Optional<File> file = Convert.toFile(kv.getValue());
                    if (file.has()) {
                        if (!file.get().isAbsolute()) {
                            File f = new File(regFile.getParentFile(), file.get().getPath());
                            kv.setValue(f);
                        }
                    }
                }
            }
        }
        
        return drv.open(opts);
    }
    
    @Override
    public void close() {
    }

    JSONObject reg() throws IOException {
        if (reg != null) {
            return reg;
        }

        BufferedReader r = new BufferedReader(new FileReader(regFile));
        try {
            return (JSONObject) JSONValue.parseWithException(r);
        }
        catch(Exception e) {
            throw new IOException("Error parsing json registry: " + regFile.getPath(), e);
        }
        finally {
            r.close();
        }
    }


}
