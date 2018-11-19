package no.digipost.signature.client.core.internal.http;

import javax.ws.rs.client.WebTarget;

public interface SignatureHttpClient {

    WebTarget signatureServiceRoot();

    WebTarget target(String url);

}
