package org.jeo.nano;

import static org.jeo.nano.NanoHTTPD.*;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jeo.data.DataRepository;
import org.jeo.map.Style;
import org.jeo.nano.NanoHTTPD.Response;

public class StyleHandler extends Handler {

    static final Pattern STYLE_URI_RE =
            Pattern.compile("/styles/([\\w-]+)/?", Pattern.CASE_INSENSITIVE);

    @Override
    public boolean canHandle(Request request, NanoServer server) {
        return match(request, STYLE_URI_RE);
    }
    
    @Override
    public Response handle(Request request, NanoServer server) throws Exception {
        Style s = findStyle(request, server.getRegistry());
        if (s == null) {
            throw new HttpException(HTTP_NOTFOUND, "no such style: " + s);
        }

        return new Response(HTTP_OK, MIME_CSS, s.toString());
    }

    Style findStyle(Request request, DataRepository data) throws IOException {
        Matcher m = (Matcher) request.getContext().get(Matcher.class);
        String s = m.group(1);
        try {
            return data.get(s, Style.class);
        }
        catch(ClassCastException e) {
            throw new HttpException(HTTP_BADREQUEST, s + " is not a style");
        }
    }

}
