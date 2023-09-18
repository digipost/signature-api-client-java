package no.digipost.signature.client.core.internal.http;

import org.apache.hc.client5.http.entity.mime.AbstractContentBody;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.io.IOCallback;

import java.io.IOException;
import java.io.OutputStream;

class IOCallbackContentBody extends AbstractContentBody {

    private final IOCallback<OutputStream> outputStreamCallback;
    private final String filename;
    private final long contentLength;


    public IOCallbackContentBody(long contentLength, ContentType contentType, String filename, IOCallback<OutputStream> outputStreamCallback) {
        super(contentType);
        this.filename = filename;
        this.outputStreamCallback = outputStreamCallback;
        this.contentLength = contentLength;
    }

    @Override
    public String getFilename() {
        return filename;
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        outputStreamCallback.execute(out);
    }

    @Override
    public long getContentLength() {
        return contentLength;
    }

}
