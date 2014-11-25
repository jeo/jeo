/* Copyright 2013 The jeo project. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jeo.cli.cmd;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.jeo.cli.JeoCLI;
import org.jeo.data.Dataset;
import org.jeo.data.Driver;
import org.jeo.data.Drivers;
import org.jeo.data.Workspace;
import org.jeo.geojson.GeoJSONWriter;
import org.jeo.map.Style;
import org.jeo.util.Key;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandNames="drivers", commandDescription="Lists avialable format drivers")
public class DriversCmd extends JeoCmd {

    @Parameter(description="drivers", required=false)
    List<String> drivers;

    @Override
    protected void doCommand(JeoCLI cli) throws Exception {
        GeoJSONWriter w = cli.newJSONWriter();

        if (drivers != null && !drivers.isEmpty()) {
            if (drivers.size() > 1) {
                w.array();
            }

            for (String driver : drivers) {
                Driver<?> drv = Drivers.find(driver);
                if (drv == null) {
                    throw new IllegalArgumentException("No such driver: " + driver);
                }

                w.object();
                printBrief(drv, w);

                w.key("type");
                Class<?> type = drv.getType();
                if (Workspace.class.isAssignableFrom(type)) {
                    w.value("workspace");
                }
                else if (Dataset.class.isAssignableFrom(type)) {
                    w.value("dataset");
                }
                else if (Style.class.isAssignableFrom(type)) {
                    w.value("style");
                }
                else {
                    w.value(drv.getType().getSimpleName());
                }

                w.key("keys").object();
                for (Key<?> key : drv.getKeys()) {
                    w.key(key.getName()).object();
                    w.key("type").value(key.getType().getSimpleName());
                    if (key.getDefault() != null) {
                        w.key("default").value(key.getDefault());
                    }
                    w.endObject();
                }
                w.endObject();

                w.endObject();
            }
            if (drivers.size() > 1) {
                w.endArray();
            }
        }
        else {
            Iterator<Driver<?>> it = Drivers.list();
            
            w.array();
            while(it.hasNext()) {
                Driver<?> drv = it.next();
                
                w.object();

                printBrief(drv, w);
                
                w.endObject();
            }
            w.endArray();
        }

    }

    void printBrief(Driver<?> drv, GeoJSONWriter w) throws IOException {
        w.key("name").value(drv.getName());
        
        w.key("aliases").array();
        for (String a : drv.getAliases()) {
            w.value(a);
        }
        w.endArray();
    }
}
