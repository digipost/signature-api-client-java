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


    private static final String PASSWORD = "password1234";

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
        return TestClientCertificateRecordingServer.sslContextPresenting(keyStore(), PASSWORD.toCharArray());
    }

    public Certificate certificate() {
        try {
            return keyStore().getCertificate(alias);
        } catch (GeneralSecurityException e) {
            throw asUnchecked(e);
        }
    }

    /**
     * Replace the JVM's default trust store with one trusting only this certificate.
     * <p>
     *
     * See also:
     * <a href="https://docs.oracle.com/javase/8/docs/technotes/guides/security/jsse/JSSERefGuide.html#InstallationAndCustomization">
     *   Java Secure Socket Extension (JSSE) Reference Guide - Customizing JSSE
     * </a>
     *
     * @return The {@link ScopedSystemProperties} representing the modified system properties,
     *         which should be {@link ScopedSystemProperties#close() closed} when the
     *         properties are no longer needed.
     */
    public ScopedSystemProperties setupTrustBySystemProperties() {
        Path trustStore = trustStoreContaining(certificate());
        return new ScopedSystemProperties(
                "javax.net.ssl.trustStore", trustStore.toAbsolutePath().toString(),
                "javax.net.ssl.trustStoreType", "JKS",
                "javax.net.ssl.trustStorePassword", PASSWORD
                );
    }


    private KeyStore keyStore() {
        try (InputStream pkcs12 = TestServerCertificate.class.getResourceAsStream(keyStoreResource)) {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(pkcs12, PASSWORD.toCharArray());
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
                trustStore.store(out, PASSWORD.toCharArray());
            }
            return trustStoreFile;
        } catch (IOException | GeneralSecurityException e) {
            throw asUnchecked(e);
        }
    }

}
