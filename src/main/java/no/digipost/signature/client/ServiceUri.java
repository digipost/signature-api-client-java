package no.digipost.signature.client;

import java.net.URI;

public enum ServiceUri {
    PRODUCTION(URI.create("https://api.signering.posten.no/api")),
    DIFI_QA(URI.create("https://api.difiqa.signering.posten.no/api")),
    DIFI_TEST(URI.create("https://api.difitest.signering.posten.no/api"));

    final URI uri;

    ServiceUri(URI uri) {
        this.uri = uri;
    }
}
