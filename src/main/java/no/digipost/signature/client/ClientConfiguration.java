package no.digipost.signature.client;

import no.digipost.signature.client.asice.ASiCEConfiguration;
import no.digipost.signature.client.asice.DocumentBundleProcessor;
import no.digipost.signature.client.asice.DumpDocumentBundleToDisk;
import no.digipost.signature.client.core.Sender;
import no.digipost.signature.client.core.SignatureJob;
import no.digipost.signature.client.core.exceptions.KeyException;
import no.digipost.signature.client.core.internal.http.AddRequestHeaderFilter;
import no.digipost.signature.client.core.internal.http.EnterpriseCertificateTrustStrategy;
import no.digipost.signature.client.core.internal.http.HttpIntegrationConfiguration;
import no.digipost.signature.client.core.internal.security.ProvidesCertificateResourcePaths;
import no.digipost.signature.client.core.internal.security.TrustStoreLoader;
import no.digipost.signature.client.core.internal.xml.JaxbMessageReaderWriterProvider;
import no.digipost.signature.client.security.KeyStoreConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.ssl.SSLContexts;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.ws.rs.core.Configurable;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.HttpHeaders;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static java.util.Arrays.asList;
import static javax.ws.rs.core.HttpHeaders.USER_AGENT;
import static no.digipost.signature.client.Certificates.TEST;
import static no.digipost.signature.client.ClientMetadata.VERSION;

public final class ClientConfiguration implements ProvidesCertificateResourcePaths, HttpIntegrationConfiguration, ASiCEConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(ClientConfiguration.class);


    private static final String JAVA_DESCRIPTION = System.getProperty("java.vendor", "unknown Java") + ", " + System.getProperty("java.version", "unknown version");

    /**
     * The {@link HttpHeaders#USER_AGENT User-Agent} header which will be included in all requests. You may include a custom part
     * using {@link Builder#includeInUserAgent(String)}.
     */
    public static final String MANDATORY_USER_AGENT = "posten-signature-api-client-java/" + VERSION + " (" + JAVA_DESCRIPTION + ")";

    /**
     * {@value #HTTP_REQUEST_RESPONSE_LOGGER_NAME} is the name of the logger which will log the HTTP requests and responses,
     * if enabled with {@link ClientConfiguration.Builder#enableRequestAndResponseLogging()}.
     */
    public static final String HTTP_REQUEST_RESPONSE_LOGGER_NAME = "no.digipost.signature.client.http.requestresponse";

    /**
     * Socket timeout is used for both requests and, if any,
     * underlying layered sockets (typically for
     * secure sockets). The default value is {@value #DEFAULT_SOCKET_TIMEOUT_MS} ms.
     */
    public static final int DEFAULT_SOCKET_TIMEOUT_MS = 10_000;

    /**
     * The default connect timeout for requests: {@value #DEFAULT_CONNECT_TIMEOUT_MS} ms.
     */
    public static final int DEFAULT_CONNECT_TIMEOUT_MS = 10_000;



    private final Configurable<? extends Configuration> jaxrsConfig;
    private final boolean preInitializeHttpClient;
    private final KeyStoreConfig keyStoreConfig;

    private final Iterable<String> certificatePaths;
    private final Optional<Sender> sender;
    private final URI signatureServiceRoot;
    private final Iterable<DocumentBundleProcessor> documentBundleProcessors;
    private final TrustStrategy serverTrustStrategy;
    private final Clock clock;

    private ClientConfiguration(
            KeyStoreConfig keyStoreConfig, Configurable<? extends Configuration> jaxrsConfig,
            Optional<Sender> sender, URI serviceRoot, Iterable<String> certificatePaths,
            Iterable<DocumentBundleProcessor> documentBundleProcessors, TrustStrategy serverTrustStrategy, boolean preInitializeHttpClient, Clock clock) {

        this.jaxrsConfig = jaxrsConfig;
        this.preInitializeHttpClient = preInitializeHttpClient;
        this.keyStoreConfig = keyStoreConfig;
        this.certificatePaths = certificatePaths;
        this.sender = sender;
        this.signatureServiceRoot = serviceRoot;
        this.documentBundleProcessors = documentBundleProcessors;
        this.serverTrustStrategy = serverTrustStrategy;
        this.clock = clock;
    }

    @Override
    public KeyStoreConfig getKeyStoreConfig() {
        return keyStoreConfig;
    }

    @Override
    public Optional<Sender> getGlobalSender() {
        return sender;
    }

    @Override
    public Iterable<DocumentBundleProcessor> getDocumentBundleProcessors() {
        return documentBundleProcessors;
    }

    @Override
    public Clock getClock() {
        return clock;
    }


    @Override
    public URI getServiceRoot() {
        return signatureServiceRoot;
    }

    @Override
    public Iterable<String> getCertificatePaths() {
        return certificatePaths;
    }


    /**
     * Get the JAX-RS {@link Configuration} based on the current state of this {@link ClientConfiguration}.
     *
     * @return the JAX-RS {@link Configuration}
     */
    @Override
    public Configuration getJaxrsConfiguration() {
        return jaxrsConfig.getConfiguration();
    }

    @Override
    public boolean preInitializeClient() {
        return preInitializeHttpClient;
    }


    @Override
    public SSLContext getSSLContext() {
        try {
            return SSLContexts.custom()
                .loadKeyMaterial(keyStoreConfig.keyStore, keyStoreConfig.privatekeyPassword.toCharArray(), (aliases, socket) -> keyStoreConfig.alias)
                .loadTrustMaterial(TrustStoreLoader.build(this), serverTrustStrategy)
                .build();
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException | UnrecoverableKeyException e) {
            if (e instanceof UnrecoverableKeyException && "Given final block not properly padded".equals(e.getMessage())) {
                throw new KeyException(
                        "Unable to load key from keystore, because " + e.getClass().getSimpleName() + ": '" + e.getMessage() + "'. Possible causes:\n" +
                        "* Wrong password for private key (the password for the keystore and the private key may not be the same)\n" +
                        "* Multiple private keys in the keystore with different passwords (private keys in the same key store must have the same password)", e);
            } else {
                throw new KeyException("Unable to create the SSLContext, because " + e.getClass().getSimpleName() + ": '" + e.getMessage() + "'", e);
            }
        }
    }



    /**
     * Build a new {@link ClientConfiguration}.
     */
    public static Builder builder(KeyStoreConfig keystore) {
        return new Builder(keystore);
    }

    public static class Builder {

        private final Configurable<? extends Configuration> jaxrsConfig;
        private final KeyStoreConfig keyStoreConfig;

        private boolean preInitializeHttpClient = true;
        private int socketTimeoutMs = DEFAULT_SOCKET_TIMEOUT_MS;
        private int connectTimeoutMs = DEFAULT_CONNECT_TIMEOUT_MS;
        private Optional<String> customUserAgentPart = Optional.empty();
        private URI serviceRoot = ServiceUri.PRODUCTION.uri;
        private Optional<Sender> globalSender = Optional.empty();
        private Iterable<String> certificatePaths = Certificates.PRODUCTION.certificatePaths;
        private TrustStrategy serverCertificateTrustStrategy = new EnterpriseCertificateTrustStrategy("984661185"); // Posten Norge AS organization number
        private Optional<LoggingFeature> loggingFeature = Optional.empty();
        private List<DocumentBundleProcessor> documentBundleProcessors = new ArrayList<>();
        private Clock clock = Clock.systemDefaultZone();


        private Builder(KeyStoreConfig keyStoreConfig) {
            this.keyStoreConfig = keyStoreConfig;
            this.jaxrsConfig = new ClientConfig();
        }

        /**
         * Set the service URI to one of the predefined environments.
         */
        public Builder serviceUri(ServiceUri environment) {
            return serviceUri(environment.uri);
        }

        /**
         * Override the service endpoint URI to a custom environment.
         */
        public Builder serviceUri(URI uri) {
            this.serviceRoot = uri;
            return this;
        }

        /**
         * Override the
         * {@link ClientConfiguration#DEFAULT_SOCKET_TIMEOUT_MS default socket timeout value}.
         */
        public Builder socketTimeoutMillis(int millis) {
            this.socketTimeoutMs = millis;
            return this;
        }

        /**
         * Override the
         * {@link ClientConfiguration#DEFAULT_CONNECT_TIMEOUT_MS default connect timeout value}.
         */
        public Builder connectTimeoutMillis(int millis) {
            this.connectTimeoutMs = millis;
            return this;
        }

        public Builder trustStore(Certificates certificates) {
            if (certificates == TEST) {
                LOG.warn("Using test certificates in trust store. This should never be done for production environments.");
            }
            return trustStore(certificates.certificatePaths);
        }


        /**
         * Override the trust store configuration to load DER-encoded certificates from the given folder(s).
         *
         * @see java.security.cert.CertificateFactory#generateCertificate(InputStream)
         */
        public Builder trustStore(String ... certificatePaths) {
            return trustStore(asList(certificatePaths));
        }

        /**
         * Override the trust store configuration to load DER-encoded certificates from the given folder(s).
         *
         * @see java.security.cert.CertificateFactory#generateCertificate(InputStream)
         */
        public Builder trustStore(Iterable<String> certificatePaths) {
            this.certificatePaths = certificatePaths;
            return this;
        }

        /**
         * Set the sender used globally for every signature job.
         * <p>
         * Use {@link no.digipost.signature.client.portal.PortalJob.Builder#withSender(Sender)} or {@link no.digipost.signature.client.direct.DirectJob.Builder#withSender(Sender)}
         * if you need to specify different senders per signature job (typically when acting as a broker on
         * behalf of multiple other organizations)
         */
        public Builder globalSender(Sender sender) {
            this.globalSender = Optional.of(sender);
            return this;
        }

        /**
         * Customize the {@link HttpHeaders#USER_AGENT User-Agent} header value to include the
         * given string.
         *
         * @param userAgentCustomPart The custom part to include in the User-Agent HTTP header.
         */
        public Builder includeInUserAgent(String userAgentCustomPart) {
            customUserAgentPart = Optional.of(userAgentCustomPart).filter(StringUtils::isNoneBlank);
            return this;
        }

        /**
         * Makes the client log the sent requests and received responses to the logger named
         * {@link ClientConfiguration#HTTP_REQUEST_RESPONSE_LOGGER_NAME}.
         */
        public Builder enableRequestAndResponseLogging() {
            loggingFeature = Optional.of(new LoggingFeature(java.util.logging.Logger.getLogger(HTTP_REQUEST_RESPONSE_LOGGER_NAME), 16 * 1024));
            return this;
        }

        /**
         * Have the library dump the generated document bundle zip files to disk before they are
         * sent to the service to create signature jobs.
         * <p>
         * The files will be given names on the format
         * <pre>{@code timestamp-[reference_from_job-]asice.zip}</pre>
         * The <em>timestamp</em> part may use a clock of your choosing, make sure to override the system clock with
         * {@link #usingClock(Clock)} before calling this method if that is desired.
         * <p>
         * The <em>reference_from_job</em> part is only included if the job is given such a reference using
         * {@link no.digipost.signature.client.direct.DirectJob.Builder#withReference(UUID) DirectJob.Builder.withReference(..)} or {@link no.digipost.signature.client.portal.PortalJob.Builder#withReference(UUID) PortalJob.Builder.withReference(..)}.
         *
         * @param directory the directory to dump to. This directory must already exist, or
         *                  creating new signature jobs will fail. Miserably.
         */
        public Builder enableDocumentBundleDiskDump(Path directory) {
            return addDocumentBundleProcessor(new DumpDocumentBundleToDisk(directory, clock));
        }


        /**
         * Add a {@link DocumentBundleProcessor} which will be passed the generated zipped document bundle
         * together with the {@link SignatureJob job} it was created for. The processor is not responsible for closing
         * the stream, as this is handled by the library itself.
         *
         * <h2>A note on performance</h2>
         * The processor is free to do what it want with the passed stream, but bear in mind that the time
         * used by a processor adds to the processing time to create signature jobs.
         *
         * @param processor the {@link DocumentBundleProcessor} which will be passed the generated zipped document bundle
         *                  together with the {@link SignatureJob job} it was created for.
         */
        public Builder addDocumentBundleProcessor(DocumentBundleProcessor processor) {
            documentBundleProcessors.add(processor);
            return this;
        }

        /**
         * This methods allows for custom configuration of JAX-RS (i.e. Jersey) if anything is
         * needed that is not already supported by the {@link ClientConfiguration.Builder}.
         * This method should not be used to configure anything that is already directly supported by the
         * {@code ClientConfiguration.Builder} API.
         * <p>
         * If you still need to use this method, consider requesting first-class support for your requirement
         * on the library's <a href="https://github.com/digipost/signature-api-client-java/issues">web site on GitHub</a>.
         *
         * @param customizer The operations to do on the JAX-RS {@link Configurable}, e.g.
         *                   {@link Configurable#register(Object) registering components}.
         */
        public Builder customizeJaxRs(Consumer<? super Configurable<? extends Configuration>> customizer) {
            customizer.accept(jaxrsConfig);
            return this;
        }


        /**
         * Override which organization number which is expected from the server's certificate.
         * By default, this is the organization number of Posten Norge AS, and should <em>not</em>
         * be overridden unless you have a specific need such as doing testing against your own
         * stubbed implementation of the Posten signering API.
         *
         * @param serverOrganizationNumber the organization number expected in the server's enterprise certificate
         */
        public Builder serverOrganizationNumber(String serverOrganizationNumber) {
            return serverCertificateTrustStrategy(new EnterpriseCertificateTrustStrategy(serverOrganizationNumber));
        }


        /**
         * Override the strategy used for validating the server's certificate. This method is mainly
         * intended for tests if you need to override (or even disable) the default
         * validation that the server identifies itself as "Posten Norge AS".
         *
         * Calling this method for a production deployment is probably <em>not</em> what you intend to do!
         *
         * @param strategy the strategy for validating the server's certificate
         */
        public Builder serverCertificateTrustStrategy(TrustStrategy strategy) {
            LOG.warn(
                    "Overriding server certificate TrustStrategy! This should NOT be done for any production deployment, " +
                    "or any integration with Posten Norge");
            this.serverCertificateTrustStrategy = strategy;
            return this;
        }

        /**
         * Allows for overriding which {@link Clock} is used to convert between Java and XML,
         * may be useful for e.g. automated tests.
         * <p>
         * Uses {@link Clock#systemDefaultZone() the best available system clock} if not specified.
         */
        public Builder usingClock(Clock clock) {
            this.clock = clock;
            return this;
        }


        /**
         * Disable the pre-initialization step of the internal HTTP client (Jersey Client) when
         * instantiating the Signature API Client.
         *
         * @see org.glassfish.jersey.client.JerseyClient#preInitialize()
         */
        public Builder disablePreInitializingHttpClient() {
            this.preInitializeHttpClient = false;
            return this;
        }

        public ClientConfiguration build() {
            jaxrsConfig.property(ClientProperties.READ_TIMEOUT, socketTimeoutMs);
            jaxrsConfig.property(ClientProperties.CONNECT_TIMEOUT, connectTimeoutMs);
            jaxrsConfig.register(MultiPartFeature.class);
            jaxrsConfig.register(JaxbMessageReaderWriterProvider.class);
            jaxrsConfig.register(new AddRequestHeaderFilter(USER_AGENT, createUserAgentString()));
            this.loggingFeature.ifPresent(jaxrsConfig::register);
            return new ClientConfiguration(
                    keyStoreConfig, jaxrsConfig, globalSender, serviceRoot, certificatePaths,
                    documentBundleProcessors, serverCertificateTrustStrategy, preInitializeHttpClient, clock);
        }

        String createUserAgentString() {
            return MANDATORY_USER_AGENT + customUserAgentPart.map(ua -> String.format(" (%s)", ua)).orElse("");
        }

    }

}
