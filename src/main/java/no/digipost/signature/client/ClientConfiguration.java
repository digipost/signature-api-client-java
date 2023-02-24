package no.digipost.signature.client;

import no.digipost.signature.client.asice.ASiCEConfiguration;
import no.digipost.signature.client.asice.DocumentBundleProcessor;
import no.digipost.signature.client.asice.DumpDocumentBundleToDisk;
import no.digipost.signature.client.core.Sender;
import no.digipost.signature.client.core.SignatureJob;
import no.digipost.signature.client.core.internal.MaySpecifySender;
import no.digipost.signature.client.core.internal.configuration.ApacheHttpClientBuilderConfigurer;
import no.digipost.signature.client.core.internal.configuration.ApacheHttpClientProxyConfigurer;
import no.digipost.signature.client.core.internal.configuration.ApacheHttpClientSslConfigurer;
import no.digipost.signature.client.core.internal.configuration.ApacheHttpClientUserAgentConfigurer;
import no.digipost.signature.client.core.internal.configuration.Configurer;
import no.digipost.signature.client.core.internal.http.HttpIntegrationConfiguration;
import no.digipost.signature.client.security.CertificateChainValidation;
import no.digipost.signature.client.security.KeyStoreConfig;
import no.digipost.signature.client.security.OrganizationNumberValidation;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import static no.digipost.signature.client.core.internal.MaySpecifySender.NO_SPECIFIED_SENDER;

public final class ClientConfiguration implements HttpIntegrationConfiguration, ASiCEConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(ClientConfiguration.class);


    private static final String JAVA_DESCRIPTION = System.getProperty("java.vendor", "unknown Java") + ", " + System.getProperty("java.version", "unknown version");


    /**
     * The {@link HttpHeaders#USER_AGENT User-Agent} header which will be included in all requests. You may include a custom part
     * using {@link Builder#includeInUserAgent(String)}.
     */
    public static final String MANDATORY_USER_AGENT = "posten-signature-api-client-java/" + ClientMetadata.VERSION + " (" + JAVA_DESCRIPTION + ")";

    /**
     * Socket timeout is used for both requests and, if any,
     * underlying layered sockets (typically for
     * secure sockets).
     */
    public static final Duration DEFAULT_SOCKET_TIMEOUT = Duration.ofSeconds(10);

    /**
     * The default connect timeout for requests.
     */
    public static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(10);



    private final KeyStoreConfig keyStoreConfig;

    private final Configurer<HttpClientBuilder> httpClientConfigurer;
    private final MaySpecifySender defaultSender;
    private final URI serviceRoot;
    private final Iterable<DocumentBundleProcessor> documentBundleProcessors;
    private final Clock clock;

    private ClientConfiguration(
            KeyStoreConfig keyStoreConfig, Configurer<HttpClientBuilder> httpClientConfigurer, MaySpecifySender defaultSender,
            URI serviceRoot, Iterable<DocumentBundleProcessor> documentBundleProcessors, Clock clock) {

        this.keyStoreConfig = keyStoreConfig;
        this.httpClientConfigurer = httpClientConfigurer;
        this.defaultSender = defaultSender;
        this.serviceRoot = serviceRoot;
        this.documentBundleProcessors = documentBundleProcessors;
        this.clock = clock;
    }

    @Override
    public KeyStoreConfig getKeyStoreConfig() {
        return keyStoreConfig;
    }

    @Override
    public MaySpecifySender getDefaultSender() {
        return defaultSender;
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
        return serviceRoot;
    }

    @Override
    public HttpClient httpClient() {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        httpClientConfigurer.applyTo(httpClientBuilder);
        return httpClientBuilder.build();
    }

    /**
     * Build a new {@link ClientConfiguration}.
     */
    public static Builder builder(KeyStoreConfig keystore) {
        return new Builder(keystore);
    }


    public static class Builder {

        private final KeyStoreConfig keyStoreConfig;

        final ApacheHttpClientUserAgentConfigurer userAgentConfigurer = new ApacheHttpClientUserAgentConfigurer(MANDATORY_USER_AGENT);
        private final ApacheHttpClientSslConfigurer sslConfigurer;
        private final ApacheHttpClientBuilderConfigurer httpClientConfigurer;
        private Configurer<HttpClientBuilder> proxyConfigurer = Configurer.notConfigured();

        private ServiceEnvironment serviceEnvironment = ServiceEnvironment.PRODUCTION;
        private MaySpecifySender defaultSender = NO_SPECIFIED_SENDER;
        private List<DocumentBundleProcessor> documentBundleProcessors = new ArrayList<>();
        private Clock clock = Clock.systemDefaultZone();


        private Builder(KeyStoreConfig keyStoreConfig) {
            this.keyStoreConfig = keyStoreConfig;
            this.sslConfigurer = new ApacheHttpClientSslConfigurer(keyStoreConfig, serviceEnvironment);
            this.httpClientConfigurer = new ApacheHttpClientBuilderConfigurer(sslConfigurer)
                    .socketTimeout(Duration.ofSeconds(10))
                    .connectTimeout(Duration.ofSeconds(10))
                    .connectionRequestTimeout(Duration.ofSeconds(10))
                    .responseArrivalTimeout(Duration.ofSeconds(10));
        }

        public Builder serviceEnvironment(ServiceEnvironment other) {
            return serviceEnvironment(current -> other);
        }

        public Builder serviceEnvironment(UnaryOperator<ServiceEnvironment> updateServiceEnvironment) {
            this.serviceEnvironment = updateServiceEnvironment.apply(this.serviceEnvironment);
            this.sslConfigurer.trust(serviceEnvironment);
            return this;
        }


        /**
         * Set the http proxy host used by the client.
         *
         * @param hostname the hostname
         * @param port the port
         */
        public Builder httpProxyHost(String hostname, int port) {
            return proxyHost(URI.create(hostname + ":" + port));
        }

        /**
         * Set URI to proxy host to be used by the client. Only the
         * scheme, host, and port of the URI is used, any other parts are ignored.
         *
         * @param proxyHostUri the proxy host URI
         */
        public Builder proxyHost(URI proxyHostUri) {
            this.proxyConfigurer = new ApacheHttpClientProxyConfigurer(HttpHost.create(proxyHostUri));
            return this;
        }

        /**
         * Set the default {@link Sender} to use if not specifying sender per signature job.
         * <p>
         * Use {@link no.digipost.signature.client.portal.PortalJob.Builder#withSender(Sender) PortalJob.Builder.withSender(..)}
         * or {@link no.digipost.signature.client.direct.DirectJob.Builder#withSender(Sender) DirectJob.Builder.withSender(..)}
         * if you need to specify different senders per signature job (typically when acting as a broker on
         * behalf of multiple other organizations)
         */
        public Builder defaultSender(Sender sender) {
            this.defaultSender = MaySpecifySender.specifiedAs(sender);
            return this;
        }

        /**
         * Customize the {@link HttpHeaders#USER_AGENT User-Agent} header value to include the
         * given string.
         *
         * @param userAgentCustomPart The custom part to include in the User-Agent HTTP header.
         */
        public Builder includeInUserAgent(String userAgentCustomPart) {
            userAgentConfigurer.append("(" + userAgentCustomPart + ")");
            return this;
        }

        /**
         * Configure timeouts used for integrating with the API.
         *
         * @param timeouts the timeouts to set
         */
        public Builder timeouts(Consumer<? super TimeoutsConfigurer> timeouts) {
            timeouts.accept(httpClientConfigurer);
            return this;
        }


        /**
         * This method allows for custom configuration of the created {@link HttpClient} if anything is
         * needed that is not already supported by other methods in {@link ClientConfiguration}.
         * <p>
         * If you still need to use this method, consider requesting first-class support for your requirement
         * on the library's <a href="https://github.com/digipost/signature-api-client-java/issues">web site on GitHub</a>.
         */
        public Builder apacheHttpClient(Consumer<? super ApacheHttpClientConfigurer> apacheHttpClientConfigurer) {
            apacheHttpClientConfigurer.accept(this.httpClientConfigurer);
            return this;
        }

        /**
         * Have the library dump the generated document bundle zip files to disk before they are
         * sent to the service to create signature jobs.
         * <p>
         * The files will be given names on the format
         * <pre>{@code timestamp-[reference_from_job-]asice.zip}</pre>
         * The <em>timestamp</em> part may use a clock of your choosing, make sure to override the system clock with
         * {@link #clock(Clock)} before calling this method if that is desired.
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
         * <p><strong>A note on performance:</strong>
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
         * Override which organization number which is expected from the server's certificate.
         * By default, this is the organization number of Posten Norge AS, and should <em>not</em>
         * be overridden unless you have a specific need such as doing testing against your own
         * stubbed implementation of the Posten signering API.
         *
         * @param serverOrganizationNumber the organization number expected in the server's enterprise certificate
         */
        public Builder serverOrganizationNumber(String serverOrganizationNumber) {
            return serverCertificateTrustStrategy(new OrganizationNumberValidation(serverOrganizationNumber));
        }


        /**
         * Override the validation of the server's certificate. This method is mainly
         * intended for tests if you need to override (or even disable) the default
         * validation that the server identifies itself as "Posten Norge AS".
         *
         * Calling this method for a production deployment is probably <em>not</em> what you intend to do!
         *
         * @param certificateChainValidation the validation for the server's certificate
         */
        public Builder serverCertificateTrustStrategy(CertificateChainValidation certificateChainValidation) {
            LOG.warn("Overriding server certificate TrustStrategy! This should NOT be done for any integration with Posten signering.");
            this.sslConfigurer.certificatChainValidation(certificateChainValidation);
            return this;
        }

        /**
         * Allows for overriding which {@link Clock} is used to convert between Java and XML,
         * may be useful for e.g. automated tests.
         * <p>
         * Uses {@link Clock#systemDefaultZone() the best available system clock} if not specified.
         */
        public Builder clock(Clock clock) {
            this.clock = clock;
            return this;
        }

        public ClientConfiguration build() {
            Configurer<HttpClientBuilder> allHttpClientConfigurers = userAgentConfigurer
                    .andThen(proxyConfigurer)
                    .andThen(httpClientConfigurer);

            return new ClientConfiguration(
                    keyStoreConfig, allHttpClientConfigurers, defaultSender, serviceEnvironment.serviceUrl(), documentBundleProcessors, clock);
        }

    }




    static final class ClientMetadata {

        static final String VERSION;

        static {
            String version = "unknown version";
            try (InputStream versionFile = ClientMetadata.class.getResourceAsStream("version"); Scanner scanner = new Scanner(versionFile, "UTF-8")) {
                version = scanner.next();
            } catch (Exception e) {
                Logger log = LoggerFactory.getLogger(ClientMetadata.class);
                log.warn("Unable to resolve library version from classpath resource 'version', because {}: '{}'", e.getClass().getSimpleName(), e.getMessage());
                if (log.isDebugEnabled()) {
                    log.debug(e.getMessage(), e);
                } else {
                    log.info("Enable debug-logging for logger '{}' to see full stacktrace for above warning" + log.getName());
                }
            } finally {
                VERSION = version;
            }
        }

        private ClientMetadata() {}
    }

}


