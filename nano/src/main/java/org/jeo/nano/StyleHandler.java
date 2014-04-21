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

import static org.jeo.nano.NanoHTTPD.*;

import java.io.IOException;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jeo.data.DataRepository;
import org.jeo.data.DataRepositoryView;
import org.jeo.data.Handle;
import org.jeo.filter.Property;
import org.jeo.filter.Self;
import org.jeo.filter.TypeOf;
import org.jeo.geojson.GeoJSONWriter;
import org.jeo.map.Style;
import org.jeo.nano.NanoHTTPD.Response;

public class StyleHandler extends Handler {

    static final Pattern STYLE_URI_RE =
            Pattern.compile("/styles(?:/([\\w-]+)/?)?", Pattern.CASE_INSENSITIVE);

    @Override
    public boolean canHandle(Request request, NanoServer server) {
        return match(request, STYLE_URI_RE);
    }
    
    @Override
    public Response handle(Request request, NanoServer server) throws Exception {
        DataRepositoryView reg = server.getRegistry();
        Matcher m = (Matcher) request.getContext().get(Matcher.class);
        String s = m.group(1);
        if (s == null) {
            return listAllStyles(reg);
        }
        else {
            Style style = findStyle(s, reg);
            if (style == null) {
                throw new HttpException(HTTP_NOTFOUND, "no such style: " + s);
            }

            return new Response(HTTP_OK, MIME_CSS, style.toString());
        }

    }

    Response listAllStyles(DataRepositoryView reg) throws IOException {
        StringWriter out = new StringWriter();
        GeoJSONWriter w = new GeoJSONWriter(out);
        w.object();

        for (Handle h : reg.query(new TypeOf<Handle<?>>(new Property("type"), Style.class))) {
            w.key(h.getName()).object()
                .key("type").value("style")
                .key("driver").value(h.getDriver().getName())
                .endObject();
        }

        w.endObject();

        return new Response(HTTP_OK, MIME_JSON, out.toString());
    }

    Style findStyle(String name, DataRepository data) throws IOException {
        try {
            return data.get(name, Style.class);
        }
        catch(ClassCastException e) {
            throw new HttpException(HTTP_BADREQUEST, name + " is not a style");
        }
    }

}
