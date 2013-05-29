package org.jeo.nano;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Request {

    String uri;
    String method;
    Properties header;
    Properties parms;
    Properties files;
    Map<Object, Object> context;

    public Request(String uri, String method, Properties header, Properties parms, Properties files) {
        this.uri = uri;
        this.method = method;
        this.header = header;
        this.parms = parms;
        this.files = files;
        this.context = new HashMap<Object, Object>();
    }

    public String getUri() {
        return uri;
    }

    public String getMethod() {
        return method;
    }

    public Properties getHeader() {
        return header;
    }

    public Properties getParms() {
        return parms;
    }

    public Properties getFiles() {
        return files;
    }

    public Map<Object, Object> getContext() {
        return context;
    }
}
