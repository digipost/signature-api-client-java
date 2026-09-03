package no.digipost.signature.client.core.internal.http;

import no.digipost.signature.client.TestClientCertificateRecordingServer;
import no.digipost.signature.client.core.exceptions.HttpIOException;
import no.digipost.signature.client.core.internal.configuration.Configurer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.net.ssl.SSLContext;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static no.digipost.signature.client.TestKonfigurasjon.CLIENT_KEYSTORE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.co.probablyfine.matchers.OptionalMatchers.contains;

/**
 * Verifies that the connection to the token endpoint really is mutually authenticated, i.e. that the
 * client certificate is presented during the TLS handshake. This is the entire premise of
 * {@link MutualTlsTokenProvider}, and only a real handshake against a real server can show it.
 *
 * <p>The token endpoint client validates the server against the <em>JVM's default trust store</em>
 * with ordinary host name verification, deliberately without the escape hatches the Posten signering
 * API client has. That is the behaviour under test, so it is not circumvented here: instead the test
 * server presents a certificate issued for {@code localhost}, and the JVM's default trust store is
 * pointed at that certificate while the client is being built.
 *
 * @see no.digipost.signature.client.ClientConfigurationClientCertificateTest
 * for the same kind of assertion on the connection to the API itself
 */
class MutualTlsTokenProviderClientCertificateTest {

    /**
     * A self signed certificate issued for {@code localhost}, valid until 2126. Regenerate with:
     * <pre>
     * keytool -genkeypair -alias localhost -keyalg RSA -keysize 2048 -validity 36500 \
     *   -storetype PKCS12 -keystore lib/src/test/resources/localhost-tls-testserver.p12 \
     *   -storepass password1234 -keypass password1234 \
     *   -dname "CN=localhost, OU=Posten signering, O=Posten signering test, L=Oslo, C=NO" \
     *   -ext "SAN=dns:localhost,ip:127.0.0.1"
     * </pre>
     */
    private static final String SERVER_KEYSTORE_RESOURCE = "/localhost-tls-testserver.p12";
    private static final String SERVER_ALIAS = "localhost";
    private static final char[] SERVER_PASSWORD = "password1234".toCharArray();

    private static final String TOKEN_RESPONSE = "{\"access_token\":\"a-token\",\"expires_in\":3600}";


    @Test
    void presentsTheClientCertificateToTheTokenEndpoint(@TempDir Path tempDirectory) throws Exception {
        KeyStore serverKeyStore = serverKeyStore();

        try (TestClientCertificateRecordingServer tokenEndpoint = startTokenEndpoint(serverKeyStore)) {
            Path trustStore = trustStoreContaining(serverKeyStore, tempDirectory);

            MutualTlsTokenProvider tokenProvider = withDefaultTrustStore(trustStore,
                    () -> MutualTlsTokenProvider.create(
                            tokenRequestTo(tokenEndpoint.baseUri()), CLIENT_KEYSTORE,
                            Configurer.notConfigured(), Clock.systemUTC()));

            assertThat(tokenProvider.getToken(), is("a-token"));

            List<Optional<X509Certificate>> presented = tokenEndpoint.presentedClientCertificates();
            assertThat(presented, hasSize(1));
            assertThat(presented.get(0), contains(CLIENT_KEYSTORE.getCertificate()));
        }
    }

    /**
     * The token endpoint is a separate service from the Posten signering API, and is not covered by
     * the API's trust configuration. It must be validated the ordinary way, which means an unknown
     * certificate is refused rather than accepted.
     */
    @Test
    void refusesATokenEndpointWhoseCertificateTheJvmDoesNotTrust() throws Exception {
        try (TestClientCertificateRecordingServer tokenEndpoint = startTokenEndpoint(serverKeyStore())) {

            // Note: no trust store override, so the self signed certificate of the test server is unknown.
            MutualTlsTokenProvider tokenProvider = MutualTlsTokenProvider.create(
                    tokenRequestTo(tokenEndpoint.baseUri()), CLIENT_KEYSTORE,
                    Configurer.notConfigured(), Clock.systemUTC());

            assertThrows(HttpIOException.class, tokenProvider::getToken);
            assertThat(tokenEndpoint.presentedClientCertificates(), hasSize(0));
        }
    }


    private static TestClientCertificateRecordingServer startTokenEndpoint(KeyStore serverKeyStore) throws Exception {
        SSLContext serverSslContext = TestClientCertificateRecordingServer.sslContextPresenting(serverKeyStore, SERVER_PASSWORD);
        return TestClientCertificateRecordingServer.start(serverSslContext, "application/json", TOKEN_RESPONSE);
    }

    private static AccessTokenRequest tokenRequestTo(URI tokenEndpointBaseUri) {
        return new AccessTokenRequest(
                URI.create(tokenEndpointBaseUri + "/token"),
                "my-client-id",
                "signering:555444",
                "https://api.example.com");
    }

    private static KeyStore serverKeyStore() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (InputStream pkcs12 = MutualTlsTokenProviderClientCertificateTest.class.getResourceAsStream(SERVER_KEYSTORE_RESOURCE)) {
            keyStore.load(pkcs12, SERVER_PASSWORD);
        }
        return keyStore;
    }

    /**
     * A trust store containing only the test server's certificate, written to a file, as the JVM's
     * default trust store is configured as a file path.
     */
    private static Path trustStoreContaining(KeyStore serverKeyStore, Path directory) throws Exception {
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(null, null);
        trustStore.setCertificateEntry("token-endpoint", serverKeyStore.getCertificate(SERVER_ALIAS));

        Path trustStoreFile = directory.resolve("token-endpoint-truststore.jks");
        try (OutputStream out = Files.newOutputStream(trustStoreFile)) {
            trustStore.store(out, SERVER_PASSWORD);
        }
        return trustStoreFile;
    }

    /**
     * Run the given action with the JVM's default trust store replaced by the given one.
     * <p>
     * The action must be the creation of the {@link MutualTlsTokenProvider} itself, and nothing more:
     * the {@link SSLContext} resolves its trust managers when it is initialized, so the replacement
     * only has to be in place while the client is built, not while it is used.
     */
    private static <T> T withDefaultTrustStore(Path trustStore, Supplier<T> action) {
        String previousPath = System.getProperty("javax.net.ssl.trustStore");
        String previousType = System.getProperty("javax.net.ssl.trustStoreType");
        String previousPassword = System.getProperty("javax.net.ssl.trustStorePassword");

        System.setProperty("javax.net.ssl.trustStore", trustStore.toAbsolutePath().toString());
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");
        System.setProperty("javax.net.ssl.trustStorePassword", new String(SERVER_PASSWORD));
        try {
            return action.get();
        } finally {
            restore("javax.net.ssl.trustStore", previousPath);
            restore("javax.net.ssl.trustStoreType", previousType);
            restore("javax.net.ssl.trustStorePassword", previousPassword);
        }
    }

    private static void restore(String property, String previousValue) {
        if (previousValue == null) {
            System.clearProperty(property);
        } else {
            System.setProperty(property, previousValue);
        }
    }

}
