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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jeo.data.Dataset;
import org.jeo.data.DataRepository;
import org.jeo.data.Workspace;
import static org.jeo.nano.NanoHTTPD.HTTP_BADREQUEST;
import static org.jeo.nano.NanoHTTPD.HTTP_NOTFOUND;
import org.jeo.nano.NanoHTTPD.Response;
import org.jeo.proj.Proj;
import org.jeo.util.Pair;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.osgeo.proj4j.UnknownAuthorityCodeException;
import org.osgeo.proj4j.UnsupportedParameterException;

public abstract class Handler {

    public void init(NanoServer server) {
    }

    public abstract boolean canHandle(Request request, NanoServer server);

    public abstract Response handle(Request request, NanoServer server) throws Exception;

    protected boolean match(Request request, Pattern pattern) {
        Matcher m = pattern.matcher(request.getUri());
        if (m.matches()) {
            //save the matcher
            request.getContext().put(Matcher.class, m);
            return true;
        }
        return false;
    }

    protected CoordinateReferenceSystem parseCRS(Properties props) {
        return parseCRS(props.getProperty("srs"));
    }

    protected CoordinateReferenceSystem parseCRS(String srs) {
        try {
            return srs == null ? null : Proj.crs(srs);
        } catch (UnsupportedParameterException upe) {
            throw new HttpException(HTTP_BADREQUEST, "Cannot locate provided srs: " + srs);
        } catch (UnknownAuthorityCodeException uace) {
            throw new HttpException(HTTP_BADREQUEST, "Cannot locate provided authority: " + srs);
        } catch (IllegalStateException iae) {
            throw new HttpException(HTTP_BADREQUEST, "Cannot locate provided srs: " + srs);
        }
    }

    protected String createPath(Request request) {
        Matcher m = (Matcher) request.getContext().get(Matcher.class);
        String second = m.group(2);
        return m.group(1) + (second == null ? "" : "/" + second);
    }

    /**
     * Find a workspace by key.
     * @param key
     * @param reg
     * @return a non-null workspace
     * @throws IOException
     * @throws HttpException a Not Found status if nothing found
     */
    protected Workspace findWorkspace(String key, DataRepository reg) throws IOException {
        Workspace workspace = reg.get(key, Workspace.class);
        if (workspace == null) {
            throw new HttpException(HTTP_NOTFOUND, "No such workspace: " + key);
        }
        return (Workspace) workspace;
    }

    /**
     * Find a workspace or dataset (and it's workspace). If only the first
     * group of the matcher contains a path, only the Workspace will be returned.
     * If neither group is supplied, null will be returned.
     * If either of the specified paths are missing, a Not Found exception will
     * be thrown.
     * 
     * @param request
     * @param reg
     * @return null or A Pair of Workspace and if requested and found, a Dataset
     * @throws IOException
     * @throws HttpException a Not Found status if nothing found
     */
    protected Pair<Workspace,? extends Dataset> findWorkspaceOrDataset(Request request, DataRepository reg) throws IOException {
        Matcher m = (Matcher) request.getContext().get(Matcher.class);
        String first = m.group(1);
        String second = m.group(2);
        Pair retval = null;
        if (first != null) {
            Workspace ws = findWorkspace(first, reg);
            Dataset ds = null;
            if (second != null) {
                ds = ws.get(second);
                if (ds == null) {
                    throw new HttpException(HTTP_NOTFOUND,
                            "no such dataset: " + second + " in workspace: " + first);
                }
            }
            retval = new Pair(ws, ds);
        }
        return retval;
    }

    protected String renderTemplate(String template, Map<String,String> vars) throws IOException {
        BufferedReader in = 
            new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(template)));
        try {
            StringBuilder sb = new StringBuilder();

            String line = null;
            while((line = in.readLine()) != null) {
                for (Map.Entry<String,String> e: vars.entrySet()) {
                    line = line.replace("%" + e.getKey() + "%", e.getValue());
                }
                sb.append(line).append("\n");
            }
            return sb.toString();
        }
        finally {
            in.close();
        }
    }
}
