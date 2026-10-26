package no.digipost.signature.client;

import javax.net.ssl.SSLContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.function.Supplier;

import static no.digipost.DiggExceptions.asUnchecked;

/**
 * Self signed certificates for test servers which clients validate with the JVM's default trust store,
 * including hostname verification. Valid until 2126. Regenerate with e.g.:
 * <pre>
 * keytool -genkeypair -alias localhost -keyalg RSA -keysize 2048 -validity 36500 \
 *   -storetype PKCS12 -keystore lib/src/test/resources/localhost-tls-testserver.p12 \
 *   -storepass password1234 -keypass password1234 \
 *   -dname "CN=localhost, OU=Posten signering, O=Posten signering test, L=Oslo, C=NO" \
 *   -ext "SAN=dns:localhost,ip:127.0.0.1"
 * </pre>
 */
public enum TestServerCertificate {

    /**
     * Issued for {@code localhost} and {@code 127.0.0.1}.
     */
    LOCALHOST("/localhost-tls-testserver.p12", "localhost"),

    /**
     * Issued for {@code other-host.example} only, i.e. fails hostname verification for a local server.
     */
    OTHER_HOSTNAME("/other-hostname-tls-testserver.p12", "other-host");


    private static final char[] PASSWORD = "password1234".toCharArray();

    private final String keyStoreResource;
    private final String alias;

    TestServerCertificate(String keyStoreResource, String alias) {
        this.keyStoreResource = keyStoreResource;
        this.alias = alias;
    }


    /**
     * For a {@link TestClientCertificateRecordingServer} presenting this certificate.
     */
    public SSLContext serverSslContext() {
        return TestClientCertificateRecordingServer.sslContextPresenting(keyStore(), PASSWORD);
    }

    public Certificate certificate() {
        try {
            return keyStore().getCertificate(alias);
        } catch (GeneralSecurityException e) {
            throw asUnchecked(e);
        }
    }

    /**
     * Replace the JVM's default trust store with one trusting only this certificate, while
     * building the client, which is when the trust managers are resolved.
     */
    public <T> T trustedByDefaultWhile(Supplier<T> buildClient) {
        Path trustStore = trustStoreContaining(certificate());
        String previousPath = System.getProperty("javax.net.ssl.trustStore");
        String previousType = System.getProperty("javax.net.ssl.trustStoreType");
        String previousPassword = System.getProperty("javax.net.ssl.trustStorePassword");

        System.setProperty("javax.net.ssl.trustStore", trustStore.toAbsolutePath().toString());
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");
        System.setProperty("javax.net.ssl.trustStorePassword", new String(PASSWORD));
        try {
            return buildClient.get();
        } finally {
            restore("javax.net.ssl.trustStore", previousPath);
            restore("javax.net.ssl.trustStoreType", previousType);
            restore("javax.net.ssl.trustStorePassword", previousPassword);
        }
    }


    private KeyStore keyStore() {
        try (InputStream pkcs12 = TestServerCertificate.class.getResourceAsStream(keyStoreResource)) {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(pkcs12, PASSWORD);
            return keyStore;
        } catch (IOException | GeneralSecurityException e) {
            throw asUnchecked(e);
        }
    }

    private static Path trustStoreContaining(Certificate trusted) {
        try {
            Path trustStoreFile = Files.createTempFile("test-truststore", ".jks");
            trustStoreFile.toFile().deleteOnExit();
            try (OutputStream out = Files.newOutputStream(trustStoreFile)) {
                KeyStore trustStore = KeyStore.getInstance("JKS");
                trustStore.load(null, null);
                trustStore.setCertificateEntry("trusted", trusted);
                trustStore.store(out, PASSWORD);
            }
            return trustStoreFile;
        } catch (IOException | GeneralSecurityException e) {
            throw asUnchecked(e);
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
