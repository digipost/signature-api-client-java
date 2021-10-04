package no.digipost.signature.client.core.internal.http;

import javax.net.ssl.SSLContext;
import javax.ws.rs.core.Configuration;

import java.net.URI;

public interface HttpIntegrationConfiguration {

    String PRE_INIT_CLIENT = "no.posten.signering.client.preInit";

    Configuration getJaxrsConfiguration();

    SSLContext getSSLContext();

    URI getServiceRoot();

}
