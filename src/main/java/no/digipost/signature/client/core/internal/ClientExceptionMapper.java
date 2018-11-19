package no.digipost.signature.client.core.internal;

import no.digipost.signature.client.core.exceptions.ConfigurationException;
import no.digipost.signature.client.core.exceptions.SignatureException;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.ws.rs.ProcessingException;
import java.util.function.Supplier;

class ClientExceptionMapper {

    void doWithMappedClientException(Runnable action) {
        doWithMappedClientException(() -> {
            action.run();
            return null;
        });
    }

    <T> T doWithMappedClientException(Supplier<T> produceResult) {
        try {
            return produceResult.get();
        } catch (ProcessingException e) {
            throw map(e);
        }
    }


    private RuntimeException map(ProcessingException e) {
        if (e.getCause() instanceof SSLException) {
            String sslExceptionMessage = e.getCause().getMessage();
            if (sslExceptionMessage != null && sslExceptionMessage.contains("protocol_version")) {
                return new ConfigurationException(
                        "Invalid TLS protocol version. This will typically happen if you're running on an older Java version, which doesn't support TLS 1.2. " +
                        "Java 7 needs to be explicitly configured to support TLS 1.2. See 'JSSE tuning parameters' at " +
                        "https://blogs.oracle.com/java-platform-group/entry/diagnosing_tls_ssl_and_https.", e);
            }
        }

        if (e.getCause() instanceof SSLHandshakeException) {
            return new SignatureException(
                    "Unable to perform SSL handshake with remote server. Some possible causes (could be others, see underlying error): \n" +
                    "* A certificate with the wrong KeyUsage was used. The keyUsage should be DigitalSignature\n" +
                    "* Erroneous configuration of the trust store\n" +
                    "* Intermediate network devices interfering with traffic (e.g. proxies)\n" +
                    "* An attacker impersonating the server (man in the middle)\n" +
                    "* Wrong TLS version. For Java 7, see 'JSSE tuning parameters' at https://blogs.oracle.com/java-platform-group/entry/diagnosing_tls_ssl_and_https for information about enabling the latest TLS versions", e);
        }

        return e;
    }

}
