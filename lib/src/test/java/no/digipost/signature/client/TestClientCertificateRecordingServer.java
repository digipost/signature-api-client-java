package no.digipost.signature.client;

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsExchange;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * A local HTTPS server which records the client certificate presented to it, if any, and answers
 * every request with the same canned response. Used to assert what a client actually puts on the
 * wire during the TLS handshake, which no amount of inspecting its configuration can prove.
 *
 * <p>The server is configured to <em>want</em>, not <em>require</em>, a client certificate.
 * Requiring one would make a client that sends none fail the handshake, which would prove only that
 * something went wrong, not that no certificate was sent.
 */
public final class TestClientCertificateRecordingServer implements AutoCloseable {

    private final HttpsServer server;
    private final List<Optional<X509Certificate>> presentedClientCertificates = new CopyOnWriteArrayList<>();

    private TestClientCertificateRecordingServer(HttpsServer server) {
        this.server = server;
    }

    /**
     * Start a server on a free port on localhost.
     *
     * @param serverSslContext the {@link SSLContext} the server presents itself with,
     *                         cf. {@link #sslContextPresenting(KeyStore, char[])}
     * @param contentType      the {@code Content-Type} of the canned response
     * @param body             the body of the canned response
     */
    public static TestClientCertificateRecordingServer start(SSLContext serverSslContext, String contentType, String body)
            throws IOException {

        HttpsServer server = HttpsServer.create(new InetSocketAddress("localhost", 0), 0);
        server.setHttpsConfigurator(new HttpsConfigurator(serverSslContext) {
            @Override
            public void configure(HttpsParameters params) {
                SSLParameters sslParameters = getSSLContext().getDefaultSSLParameters();
                sslParameters.setWantClientAuth(true);
                params.setSSLParameters(sslParameters);
            }
        });

        TestClientCertificateRecordingServer recordingServer = new TestClientCertificateRecordingServer(server);
        byte[] responseBody = body.getBytes(UTF_8);
        server.createContext("/", exchange -> {
            recordingServer.presentedClientCertificates.add(clientCertificateOf((HttpsExchange) exchange));

            // The request body must be consumed before the response can be written.
            try (InputStream request = exchange.getRequestBody()) {
                byte[] discarded = new byte[4096];
                while (request.read(discarded) != -1) {
                    // just draining
                }
            }

            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, responseBody.length);
            try (OutputStream response = exchange.getResponseBody()) {
                response.write(responseBody);
            }
        });
        server.start();
        return recordingServer;
    }

    /**
     * An {@link SSLContext} presenting the certificate of the given key store, and accepting any
     * client certificate. The point of this server is to record what the client presented, not to
     * judge it.
     */
    public static SSLContext sslContextPresenting(KeyStore keyStore, char[] keyPassword) throws GeneralSecurityException {
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, keyPassword);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(), new TrustManager[]{acceptAnyClientCertificate()}, null);
        return sslContext;
    }

    /**
     * The base URI of this server, using the host name {@code localhost}. Clients which verify the
     * server's host name will need its certificate to be issued for that name.
     */
    public URI baseUri() {
        return URI.create("https://localhost:" + server.getAddress().getPort());
    }

    /**
     * The client certificate presented for each request the server has received, in order, empty for
     * the requests where the client presented none.
     */
    public List<Optional<X509Certificate>> presentedClientCertificates() {
        return presentedClientCertificates;
    }

    @Override
    public void close() {
        server.stop(0);
    }


    private static Optional<X509Certificate> clientCertificateOf(HttpsExchange exchange) {
        try {
            Certificate[] peerCertificates = exchange.getSSLSession().getPeerCertificates();
            return Optional.of((X509Certificate) peerCertificates[0]);
        } catch (SSLPeerUnverifiedException e) {
            // Which is how the JVM communicates that the client presented no certificate at all.
            return Optional.empty();
        }
    }

    private static TrustManager acceptAnyClientCertificate() {
        return new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
    }

}
