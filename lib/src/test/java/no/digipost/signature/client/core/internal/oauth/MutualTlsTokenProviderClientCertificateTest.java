package no.digipost.signature.client.core.internal.oauth;

import no.digipost.signature.client.TestClientCertificateRecordingServer;
import no.digipost.signature.client.core.exceptions.HttpIOException;
import no.digipost.signature.client.core.internal.configuration.Configurer;
import no.digipost.signature.client.security.oauth.ClientId;
import no.digipost.signature.client.security.oauth.OAuthConfiguration;
import no.digipost.signature.client.security.oauth.Resource;
import no.digipost.signature.client.security.oauth.Scope;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.net.ssl.SSLContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static no.digipost.DiggExceptions.asUnchecked;
import static no.digipost.signature.client.TestKonfigurasjon.CLIENT_KEYSTORE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.co.probablyfine.matchers.OptionalMatchers.contains;

/**
 * Verifies that the client certificate is presented to the token endpoint. The client uses the
 * JVM's default trust store, so it is pointed at the test server's {@code localhost} certificate.
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
    void presentsTheClientCertificateToTheTokenEndpoint(@TempDir Path tempDirectory) {
        KeyStore serverKeyStore = serverKeyStore();

        try (TestClientCertificateRecordingServer tokenEndpoint = startTokenEndpoint(serverKeyStore)) {
            Path trustStore = trustStoreContaining(serverKeyStore, tempDirectory);

            MutualTlsTokenProvider tokenProvider = withDefaultTrustStore(trustStore,
                    () -> MutualTlsTokenProvider.create(
                            oAuthConfiguration(tokenEndpoint.baseUri()), CLIENT_KEYSTORE,
                            Configurer.notConfigured(), Clock.systemUTC()));

            assertThat(tokenProvider.getToken(), is("a-token"));

            List<Optional<X509Certificate>> presented = tokenEndpoint.presentedClientCertificates();
            assertThat(presented, hasSize(1));
            assertThat(presented.get(0), contains(CLIENT_KEYSTORE.getCertificate()));
        }
    }

    /**
     * The token endpoint is not covered by the API's trust configuration.
     */
    @Test
    void refusesATokenEndpointWhoseCertificateTheJvmDoesNotTrust() {
        try (TestClientCertificateRecordingServer tokenEndpoint = startTokenEndpoint(serverKeyStore())) {

            // No trust store override, so the test server's certificate is unknown
            MutualTlsTokenProvider tokenProvider = MutualTlsTokenProvider.create(
                    oAuthConfiguration(tokenEndpoint.baseUri()), CLIENT_KEYSTORE,
                    Configurer.notConfigured(), Clock.systemUTC());

            assertThrows(HttpIOException.class, tokenProvider::getToken);
            assertThat(tokenEndpoint.presentedClientCertificates(), hasSize(0));
        }
    }


    private static TestClientCertificateRecordingServer startTokenEndpoint(KeyStore serverKeyStore) {
        SSLContext serverSslContext = TestClientCertificateRecordingServer.sslContextPresenting(serverKeyStore, SERVER_PASSWORD);
        return TestClientCertificateRecordingServer.start(serverSslContext, "application/json", TOKEN_RESPONSE);
    }

    private static OAuthConfiguration oAuthConfiguration(URI tokenEndpointBaseUri) {
        return new OAuthConfiguration(
                URI.create(tokenEndpointBaseUri + "/token"),
                new Resource("https://api.example.com"),
                new ClientId("my-client-id"),
                Scope.fromAccountId(555444));
    }

    private static KeyStore serverKeyStore() {
        try (InputStream pkcs12 = MutualTlsTokenProviderClientCertificateTest.class.getResourceAsStream(SERVER_KEYSTORE_RESOURCE)) {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(pkcs12, SERVER_PASSWORD);
            return keyStore;
        } catch (IOException | GeneralSecurityException e) {
            throw asUnchecked(e);
        }
    }

    private static Path trustStoreContaining(KeyStore serverKeyStore, Path directory) {
        Path trustStoreFile = directory.resolve("token-endpoint-truststore.jks");
        try (OutputStream out = Files.newOutputStream(trustStoreFile)) {
            KeyStore trustStore = KeyStore.getInstance("JKS");
            trustStore.load(null, null);
            trustStore.setCertificateEntry("token-endpoint", serverKeyStore.getCertificate(SERVER_ALIAS));
            trustStore.store(out, SERVER_PASSWORD);
        } catch (IOException | GeneralSecurityException e) {
            throw asUnchecked(e);
        }
        return trustStoreFile;
    }

    /**
     * Replace the JVM's default trust store while building the client, which is when the trust managers are resolved.
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
