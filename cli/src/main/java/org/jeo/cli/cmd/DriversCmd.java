package org.jeo.cli.cmd;

import java.util.Iterator;

import org.jeo.cli.JeoCLI;
import org.jeo.data.Driver;
import org.jeo.data.Drivers;
import org.jeo.geojson.GeoJSONWriter;

import com.beust.jcommander.Parameters;

@Parameters(commandNames="drivers", commandDescription="Lists avialable format drivers")
public class DriversCmd extends JeoCmd {

    @Override
    protected void doCommand(JeoCLI cli) throws Exception {
        Iterator<Driver<?>> it = Drivers.list();

        GeoJSONWriter w = new GeoJSONWriter(cli.getConsole().getOutput(), 2);
        w.array();
        while(it.hasNext()) {
            Driver<?> drv = it.next();
            w.object();

            w.key("name").value(drv.getName());
             
            w.key("aliases").array();
            for (String a : drv.getAliases()) {
                w.value(a);
            }
            w.endArray();
            w.endObject();
        }
        w.endArray();
    }

}
