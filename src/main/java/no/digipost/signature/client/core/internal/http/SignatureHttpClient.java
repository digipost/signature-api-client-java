package no.digipost.signature.client.core.internal.http;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;

public interface SignatureHttpClient {

    URI signatureServiceRoot();

    HttpClient httpClient();

    Duration socketTimeout();

}
