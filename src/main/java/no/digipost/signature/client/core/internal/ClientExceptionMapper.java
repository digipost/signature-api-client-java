package no.digipost.signature.client.core.internal;

import no.digipost.signature.client.core.exceptions.SignatureException;

import javax.net.ssl.SSLHandshakeException;
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
        } catch (RuntimeException e) {
            throw map(e);
        }
    }


    private RuntimeException map(RuntimeException e) {
        if (e.getCause() instanceof SSLHandshakeException) {
            return new SignatureException(
                    "Unable to perform SSL handshake with remote server. Some possible causes (could be others, see underlying error): \n" +
                    "* A certificate with the wrong KeyUsage was used. The keyUsage should be DigitalSignature\n" +
                    "* Erroneous configuration of the trust store\n" +
                    "* Intermediate network devices interfering with traffic (e.g. proxies)\n" +
                    "* An attacker impersonating the server (man in the middle)." +
                    "* Wrong TLS version. For Java 7, see 'JSSE tuning parameters' at https://blogs.oracle.com/java-platform-group/entry/diagnosing_tls_ssl_and_https " +
                    "for information about enabling the latest TLS versions." +
                    "* Incorrect certificate. If none of the errors above fixes the issue, it may be because wrong certificate is being used. Please see the Posten" +
                    " signering documentation for buying and installing enterprise certificates. \n"
                    , e);
        }

        return e;
    }

}
