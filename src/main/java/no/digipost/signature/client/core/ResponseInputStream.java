package no.digipost.signature.client.core;

import java.io.FilterInputStream;
import java.io.InputStream;

public class ResponseInputStream extends FilterInputStream {

    private final int contentLength;

    public ResponseInputStream(InputStream in, int contentLength) {
        super(in);
        this.contentLength = contentLength;
    }

    public int getContentLength() {
        return contentLength;
    }
}
