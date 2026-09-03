package no.digipost.signature.client;

import no.digipost.signature.client.archive.ArchiveClient;
import no.digipost.signature.client.asice.ASiCEConfiguration;
import no.digipost.signature.client.asice.DocumentBundleProcessor;
import no.digipost.signature.client.asice.DumpDocumentBundleToDisk;
import no.digipost.signature.client.core.Sender;
import no.digipost.signature.client.core.SignatureJob;
import no.digipost.signature.client.core.WithSignatureServiceRootUrl;
import no.digipost.signature.client.core.exceptions.ConfigurationException;
import no.digipost.signature.client.core.internal.MaySpecifySender;
import no.digipost.signature.client.core.internal.configuration.ApacheHttpClientBearerTokenConfigurer;
import no.digipost.signature.client.core.internal.configuration.ApacheHttpClientBuilderConfigurer;
import no.digipost.signature.client.core.internal.configuration.ApacheHttpClientProxyConfigurer;
import no.digipost.signature.client.core.internal.configuration.ApacheHttpClientSslConfigurer;
import no.digipost.signature.client.core.internal.configuration.ApacheHttpClientUserAgentConfigurer;
import no.digipost.signature.client.core.internal.configuration.Configurer;
import no.digipost.signature.client.core.internal.http.AccessTokenRequest;
import no.digipost.signature.client.core.internal.http.MutualTlsTokenProvider;
import no.digipost.signature.client.security.CertificateChainValidation;
import no.digipost.signature.client.security.JwtAuthConfig;
import no.digipost.signature.client.security.KeyStoreConfig;
import no.digipost.signature.client.security.OrganizationNumberValidation;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpHost;

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
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.Objects.requireNonNull;
import static no.digipost.signature.client.core.internal.MaySpecifySender.NO_SPECIFIED_SENDER;

public final class ClientConfiguration implements ASiCEConfiguration, WithSignatureServiceRootUrl, ArchiveClient.Configuration {

    private static final Logger LOG = Logger.getLogger(ClientConfiguration.class.getName());


    private static final String JAVA_DESCRIPTION = System.getProperty("java.vendor", "unknown Java") + ", " + System.getProperty("java.version", "unknown version");


    /**
     * The {@link HttpHeaders#USER_AGENT User-Agent} header which will be included in all requests. You may include a custom part
     * using {@link Builder#includeInUserAgent(String)}.
     */
    public static final String MANDATORY_USER_AGENT = "posten-signature-api-client-java/" + ClientMetadata.VERSION + " (" + JAVA_DESCRIPTION + ")";


    /**
     * Prefix of the OAuth 2.0 {@code scope} which access tokens are requested for when using
     * {@link Builder#jwtAuthentication(JwtAuthConfig) JWT/mTLS authentication}, completed with the
     * {@link no.digipost.signature.client.security.BrokerId broker id} of the {@link JwtAuthConfig}.
     * <p>
     * <strong>Note:</strong> this value is a contract with the identity provider issuing the access
     * tokens, which matches it as an exact string. It is not defined by this library, and should not
     * be changed without coordinating with the identity provider.
     */
    static final String ACCESS_TOKEN_SCOPE_PREFIX = "signering:";

    private final MaySpecifySender defaultSender;
    private final URI serviceRoot;
    private final KeyStoreConfig keyStoreConfig;
    private final Configurer<HttpClientBuilder> defaultHttpClientConfigurer;
    private final Configurer<HttpClientBuilder> httpClientForDocumentDownloadsConfigurer;
    private final Iterable<DocumentBundleProcessor> documentBundleProcessors;
    private final Clock clock;



    private ClientConfiguration(
            MaySpecifySender defaultSender, URI serviceRoot, KeyStoreConfig keyStoreConfig,
            Configurer<HttpClientBuilder> defaultHttpClientConfigurer,
            Configurer<HttpClientBuilder> httpClientForDocumentDownloadsConfigurer,
            Iterable<DocumentBundleProcessor> documentBundleProcessors, Clock clock) {

        this.keyStoreConfig = keyStoreConfig;
        this.defaultSender = defaultSender;
        this.serviceRoot = serviceRoot;
        this.defaultHttpClientConfigurer = defaultHttpClientConfigurer;
        this.httpClientForDocumentDownloadsConfigurer = httpClientForDocumentDownloadsConfigurer;
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
    public URI signatureServiceRootUrl() {
        return serviceRoot;
    }


    @Override
    public HttpClient httpClientForDocumentDownloads() {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        httpClientForDocumentDownloadsConfigurer.applyTo(httpClientBuilder);
        return httpClientBuilder.build();
    }

    public HttpClient defaultHttpClient() {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        defaultHttpClientConfigurer.applyTo(httpClientBuilder);
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
        private final ApacheHttpClientBuilderConfigurer defaultHttpClientConfigurer;
        private final ApacheHttpClientBuilderConfigurer httpClientForDocumentDownloadsConfigurer;
        private Configurer<HttpClientBuilder> proxyConfigurer = Configurer.notConfigured();

        private ServiceEnvironment serviceEnvironment = ServiceEnvironment.PRODUCTION;
        private MaySpecifySender defaultSender = NO_SPECIFIED_SENDER;
        private List<DocumentBundleProcessor> documentBundleProcessors = new ArrayList<>();
        private Clock clock = Clock.systemDefaultZone();
        private JwtAuthConfig jwtAuthConfig;


        private Builder(KeyStoreConfig keyStoreConfig) {
            this.keyStoreConfig = keyStoreConfig;
            this.sslConfigurer = new ApacheHttpClientSslConfigurer(keyStoreConfig, serviceEnvironment);
            this.defaultHttpClientConfigurer = new ApacheHttpClientBuilderConfigurer()
                    .connectionManager(sslConfigurer)
                    .socketTimeout(Duration.ofSeconds(5))
                    .connectTimeout(Duration.ofSeconds(5))
                    .connectionRequestTimeout(Duration.ofSeconds(5))
                    .responseArrivalTimeout(Duration.ofSeconds(10));
            this.httpClientForDocumentDownloadsConfigurer = new ApacheHttpClientBuilderConfigurer()
                    .connectionManager(sslConfigurer)
                    .socketTimeout(Duration.ofSeconds(5))
                    .connectTimeout(Duration.ofSeconds(10))
                    .connectionRequestTimeout(Duration.ofSeconds(10))
                    .responseArrivalTimeout(Duration.ofSeconds(60));
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
         * Authenticate with Posten signering using an OAuth 2.0 <em>client credentials</em> grant
         * over a mutually authenticated TLS connection, instead of relying on the organization
         * certificate alone. Access tokens are acquired from the token endpoint given by the
         * {@link JwtAuthConfig}, and sent as an {@code Authorization: Bearer} header on all
         * requests to the API.
         *
         * <p>The organization certificate passed to {@link ClientConfiguration#builder(KeyStoreConfig)}
         * is still required, and is used to authenticate against the token endpoint.
         *
         * <p>Access tokens are acquired as the {@link no.digipost.signature.client.security.BrokerId
         * broker} of the given configuration, for the entire lifetime of the client. This is
         * independent of which {@link Sender sender} a signature job is created on behalf of: a
         * broker permitted to act on behalf of several organizations specifies that per job as
         * before, and {@link #defaultSender(Sender) defaultSender(..)} remains optional.
         *
         * @param jwtAuthConfig the client id and broker id to acquire access tokens with
         */
        public Builder jwtAuthentication(JwtAuthConfig jwtAuthConfig) {
            requireNonNull(jwtAuthConfig, "jwtAuthConfig");
            this.jwtAuthConfig = jwtAuthConfig;
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
            timeouts.accept(defaultHttpClientConfigurer);
            return this;
        }

        /**
         * Configure timeouts used for downloading documents.
         * The values for these timeouts should in general be configured higher than
         * for {@link #timeouts(Consumer) default timeouts}.
         *
         * @param timeouts the timeouts to set
         */
        public Builder timeoutsForDocumentDownloads(Consumer<? super TimeoutsConfigurer> timeouts) {
            timeouts.accept(httpClientForDocumentDownloadsConfigurer);
            return this;
        }


        /**
         * Configure the pool used by the client to manage connections
         * used for integrating with the API.
         *
         * @param connectionPool the {@link ConnectionPoolConfigurer connection pool configuration API}
         */
        public Builder connectionPool(Consumer<? super ConnectionPoolConfigurer> connectionPool) {
            connectionPool.accept(defaultHttpClientConfigurer);
            return this;
        }


        /**
         * Configure the pool used by the client to manage connections
         * used specifically for downloading documents.
         *
         * @param connectionPool the {@link ConnectionPoolConfigurer connection pool configuration API}
         */
        public Builder connectionPoolForDocumentDownloads(Consumer<? super ConnectionPoolConfigurer> connectionPool) {
            connectionPool.accept(httpClientForDocumentDownloadsConfigurer);
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
            apacheHttpClientConfigurer.accept(this.defaultHttpClientConfigurer);
            return this;
        }

        /**
         * This method allows for custom configuration of the created {@link HttpClient} if anything is
         * needed that is not already supported by other methods in {@link ClientConfiguration}.
         * <p>
         * If you still need to use this method, consider requesting first-class support for your requirement
         * on the library's <a href="https://github.com/digipost/signature-api-client-java/issues">web site on GitHub</a>.
         */
        public Builder apacheHttpClientForDocumentDownloads(Consumer<? super ApacheHttpClientConfigurer> apacheHttpClientConfigurer) {
            apacheHttpClientConfigurer.accept(this.httpClientForDocumentDownloadsConfigurer);
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
         * By default, this is the organization number of Posten Bring AS, and should <em>not</em>
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
         * validation that the server identifies itself as "Posten Bring AS".
         *
         * Calling this method for a production deployment is probably <em>not</em> what you intend to do!
         *
         * @param certificateChainValidation the validation for the server's certificate
         */
        public Builder serverCertificateTrustStrategy(CertificateChainValidation certificateChainValidation) {
            LOG.warning("Overriding server certificate TrustStrategy! This should NOT be done for any integration with Posten signering.");
            this.sslConfigurer.certificatChainValidation(certificateChainValidation);
            return this;
        }

        /**
         * Allows for overriding which {@link Clock} is used to convert between Java and XML,
         * may be useful for e.g. automated tests. The clock value is also passed on to
         * access token expiry validation for jwt functionality.
         * <p>
         * Uses the {@link Clock#systemDefaultZone() system clock with default time zone}
         * if not specified.
         */
        public Builder clock(Clock clock) {
            this.clock = clock;
            return this;
        }

        public ClientConfiguration build() {
            Configurer<HttpClientBuilder> commonConfig = userAgentConfigurer.andThen(proxyConfigurer);

            Configurer<HttpClientBuilder> apiConfig = commonConfig;
            if (jwtAuthConfig != null) {
                // The certificate authenticates this client to the token endpoint only. Requests to the API
                // authenticate with the access token, and must not present a client certificate. This applies
                // to both API clients, as they share the same ssl configurer.
                //
                // Note that this configures the builder, not the ClientConfiguration being built: the ssl
                // configurer is applied lazily, when the http clients are created. Any ClientConfiguration
                // previously built by this builder will therefore also stop presenting the certificate. That is
                // acceptable, as a builder is expected to be used to build one configuration, and enabling
                // authentication for some clients but not others is not a meaningful thing to do.
                sslConfigurer.withoutClientCertificate();

                AccessTokenRequest accessTokenRequest = new AccessTokenRequest(
                        resolveTokenEndpoint(),
                        jwtAuthConfig.clientId,
                        accessTokenScope(),
                        accessTokenResource()
                );

                // The token endpoint client is deliberately configured with commonConfig only, i.e. before the
                // bearer token configurer is added below. It must not attempt to authenticate itself with a
                // bearer token, as acquiring one is exactly what it is used for.
                MutualTlsTokenProvider tokenProvider = MutualTlsTokenProvider.create(
                        accessTokenRequest, keyStoreConfig, commonConfig, clock);
                apiConfig = commonConfig.andThen(new ApacheHttpClientBearerTokenConfigurer(tokenProvider));
            }

            return new ClientConfiguration(defaultSender,
                    serviceEnvironment.signatureServiceRootUrl(),
                    keyStoreConfig,
                    apiConfig.andThen(defaultHttpClientConfigurer),
                    apiConfig.andThen(httpClientForDocumentDownloadsConfigurer),
                    documentBundleProcessors,
                    clock
            );
        }

        /**
         * The endpoint to acquire access tokens from, which belongs to the configured
         * {@link ServiceEnvironment}.
         */
        private URI resolveTokenEndpoint() {
            return serviceEnvironment.tokenEndpoint().orElseThrow(() -> new ConfigurationException(
                    "No token endpoint to acquire access tokens from. The " + serviceEnvironment + " does not have " +
                    "one, which is expected for custom environments. Specify it with " +
                    "serviceEnvironment(env -> env.withTokenEndpoint(..))."));
        }

        /**
         * The resource the access token is requested for, which is the root URL of the API itself.
         * The identity provider matches this value as an exact string.
         */
        private String accessTokenResource() {
            return serviceEnvironment.signatureServiceRootUrl().toString();
        }

        /**
         * The scope to request access tokens for. The format of this string is a contract with the
         * identity provider issuing the tokens, and is <em>not</em> defined by this library.
         */
        private String accessTokenScope() {
            return ACCESS_TOKEN_SCOPE_PREFIX + jwtAuthConfig.brokerId.value();
        }

    }



    static final class ClientMetadata {

        static final String VERSION;

        static {
            String version = "unknown version";
            try (InputStream versionFile = ClientMetadata.class.getResourceAsStream("version"); Scanner scanner = new Scanner(versionFile, "UTF-8")) {
                version = scanner.next();
            } catch (Exception e) {
                Logger log = Logger.getLogger(ClientMetadata.class.getName());
                log.warning(
                        "Unable to resolve library version from classpath resource 'version', because " + e.getClass().getSimpleName() + ": '" + e.getMessage() + "'" +
                        (log.isLoggable(Level.FINE) ? "" : ". Enable debug-logging for logger '" + log.getName() + "' to see full stacktrace for this warning"));
                if (log.isLoggable(Level.FINE)) {
                    log.log(Level.FINE, e, e::getMessage);
                }
            } finally {
                VERSION = version;
            }
        }

        private ClientMetadata() {}
    }

}


