package org.jeo.nano;

import org.jeo.nano.NanoHTTPD.Response;

public class HttpException extends RuntimeException {

    /** serialVersionUID */
    private static final long serialVersionUID = 1L;

    String status;
    String mimeType;
    String content;

    public HttpException(String status, String content) {
        this(status, content, NanoHTTPD.MIME_PLAINTEXT);
    }
    public HttpException(String status, String content, String mimeType) {
        this.status = status;
        this.content = content;
        this.mimeType = mimeType;
    }

    public Response toResponse() {
        return new Response(status, mimeType, content);
    }
}
