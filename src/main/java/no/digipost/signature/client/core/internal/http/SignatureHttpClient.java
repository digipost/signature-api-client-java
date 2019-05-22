package no.digipost.signature.client.core.internal.http;

import javax.ws.rs.client.WebTarget;

import java.net.URI;

public interface SignatureHttpClient {

    WebTarget signatureServiceRoot();

    WebTarget target(URI uri);

}
