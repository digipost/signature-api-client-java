package no.digipost.signature.client.core;

import java.io.FilterInputStream;
import java.io.InputStream;

public class ResponseInputStream extends FilterInputStream {

    private final long contentLength;

    public ResponseInputStream(InputStream in, long contentLength) {
        super(in);
        this.contentLength = contentLength;
    }

    public long getContentLength() {
        return contentLength;
    }
}
