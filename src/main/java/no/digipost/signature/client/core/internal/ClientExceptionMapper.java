package no.digipost.signature.client.core.internal;

import no.digipost.signature.client.core.exceptions.ConfigurationException;

import javax.net.ssl.SSLException;
import javax.ws.rs.ProcessingException;

class ClientExceptionMapper {

    public RuntimeException map(ProcessingException e) {
        if (e.getCause() instanceof SSLException) {
            String sslExceptionMessage = e.getCause().getMessage();
            if (sslExceptionMessage != null && sslExceptionMessage.contains("protocol_version")) {
                return new ConfigurationException("Invalid TLS protocol version. This will typically happen if you're running on an older Java version, which doesn't support TLS 1.2. " +
                        "Java 7 needs to be explicitly configured to support TLS 1.2. See 'JSSE tuning parameters' at https://blogs.oracle.com/java-platform-group/entry/diagnosing_tls_ssl_and_https.", e);
            }
        }
        return e;
    }

}
