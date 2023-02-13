package no.digipost.signature.client.core.internal.http;

import org.apache.hc.client5.http.classic.HttpClient;

import java.net.URI;

public interface SignatureHttpClient {

    URI signatureServiceRoot();

    HttpClient httpClient();

}
