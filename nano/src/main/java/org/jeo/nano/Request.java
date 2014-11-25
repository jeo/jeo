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

    public Request(String uri, String method) {
        this(uri, method, null, null, null);
    }

    /**
     * Short cut to create a Request with the given uri, method and query.
     * @param uri
     * @param method
     * @param parms
     */
    public Request(String uri, String method, Properties parms) {
        this(uri, method, null, parms, null);
    }

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

    /**
     * Returns the base url (host + port) from the perspective of the client.
     */
    public String baseURL() {
        return header.getProperty("host");
    }

}
