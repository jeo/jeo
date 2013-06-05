package org.jeo.mongo;


public class GeoJSONMapper extends DefaultMapper {

    public GeoJSONMapper() {
        super(new Mapping().geometry("geometry").properties("properties"));
    }

}
