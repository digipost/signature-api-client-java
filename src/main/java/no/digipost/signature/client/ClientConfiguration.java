/**
 * Copyright (C) Posten Norge AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package no.digipost.signature.client;

import no.digipost.signature.client.core.Sender;
import no.digipost.signature.client.core.exceptions.KeyException;
import no.digipost.signature.client.core.internal.http.AddRequestHeaderFilter;
import no.digipost.signature.client.core.internal.http.PostenEnterpriseCertificateStrategy;
import no.digipost.signature.client.core.internal.http.ProvidesHttpIntegrationConfiguration;
import no.digipost.signature.client.core.internal.security.KeyStoreConfig;
import no.digipost.signature.client.core.internal.security.ProvidesCertificateResourcePaths;
import no.digipost.signature.client.core.internal.security.TrustStoreLoader;
import no.digipost.signature.client.core.internal.xml.JaxbMessageReaderWriterProvider;
import no.digipost.signature.client.direct.DirectJob;
import no.digipost.signature.client.portal.PortalJob;
import no.motif.f.Do;
import no.motif.single.Optional;
import org.apache.http.ssl.PrivateKeyDetails;
import org.apache.http.ssl.PrivateKeyStrategy;
import org.apache.http.ssl.SSLContexts;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.ws.rs.core.Configurable;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.HttpHeaders;

import java.io.InputStream;
import java.net.Socket;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.Map;

import static java.util.Arrays.asList;
import static javax.ws.rs.core.HttpHeaders.USER_AGENT;
import static no.digipost.signature.client.Certificates.TEST;
import static no.digipost.signature.client.Certificates.getCertificatePaths;
import static no.digipost.signature.client.ClientMetadata.VERSION;
import static no.motif.Singular.*;
import static no.motif.Strings.*;

public final class ClientConfiguration implements ProvidesCertificateResourcePaths, ProvidesHttpIntegrationConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(ClientConfiguration.class);


    private static final String JAVA_DESCRIPTION = System.getProperty("java.vendor", "unknown Java") + ", " + System.getProperty("java.version", "unknown version");

    /**
     * The {@link HttpHeaders#USER_AGENT User-Agent} header which will be included in all requests. You may include a custom part
     * using {@link Builder#includeInUserAgent(String)}.
     */
    public static final String MANDATORY_USER_AGENT = "Posten signering Java API Client/" + VERSION + " (" + JAVA_DESCRIPTION + ")";

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
    private final KeyStoreConfig keyStoreConfig;

    private final Iterable<String> certificatePaths;
    private final Optional<Sender> sender;
    private final URI signatureServiceRoot;


    private ClientConfiguration(
            KeyStoreConfig keyStoreConfig, Configurable<? extends Configuration> jaxrsConfig,
            Optional<Sender> sender, URI serviceRoot, Iterable<String> certificatePaths) {

        this.keyStoreConfig = keyStoreConfig;
        this.jaxrsConfig = jaxrsConfig;
        this.sender = sender;
        this.signatureServiceRoot = serviceRoot;
        this.certificatePaths = certificatePaths;
    }



    public KeyStoreConfig getKeyStoreConfig() {
        return keyStoreConfig;
    }

    public Optional<Sender> getGlobalSender() {
        return sender;
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
    public SSLContext getSSLContext() {
        try {
        return SSLContexts.custom()
                .loadKeyMaterial(keyStoreConfig.keyStore, keyStoreConfig.privatekeyPassword.toCharArray(), new PrivateKeyStrategy() {
                    @Override
                    public String chooseAlias(Map<String, PrivateKeyDetails> aliases, Socket socket) {
                        return keyStoreConfig.alias;
                    }
                })
                .loadTrustMaterial(TrustStoreLoader.build(this), new PostenEnterpriseCertificateStrategy())
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

        private int socketTimeoutMs = DEFAULT_SOCKET_TIMEOUT_MS;
        private int connectTimeoutMs = DEFAULT_CONNECT_TIMEOUT_MS;
        private Optional<String> customUserAgentPart = none();
        private URI serviceRoot = ServiceUri.PRODUCTION.uri;
        private Optional<Sender> globalSender = none();
        private Iterable<String> certificatePaths = Certificates.PRODUCTION.certificatePaths;
        private Optional<LoggingFilter> loggingFilter = none();


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
            return trustStore(the(certificates).split(getCertificatePaths));
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
         * Use {@link PortalJob.Builder#withSender(Sender)} or {@link DirectJob.Builder#withSender(Sender)}
         * if you need to specify different senders per signature job (typically when acting as a broker on
         * behalf of multiple other organizations)
         */
        public Builder globalSender(Sender sender) {
            this.globalSender = optional(sender);
            return this;
        }

        /**
         * Customize the {@link HttpHeaders#USER_AGENT User-Agent} header value to include the
         * given string.
         *
         * @param userAgentCustomPart The custom part to include in the User-Agent HTTP header.
         */
        public Builder includeInUserAgent(String userAgentCustomPart) {
            customUserAgentPart = optional(nonblank, userAgentCustomPart);
            return this;
        }

        /**
         * Makes the client log the sent requests and received responses to the logger named
         * {@link ClientConfiguration#HTTP_REQUEST_RESPONSE_LOGGER_NAME}.
         */
        public Builder enableRequestAndResponseLogging() {
            loggingFilter = the(new LoggingFilter(java.util.logging.Logger.getLogger(HTTP_REQUEST_RESPONSE_LOGGER_NAME), true)).asOptional();
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
        public Builder customizeJaxRs(Do<? super Configurable<? extends Configuration>> customizer) {
            customizer.with(jaxrsConfig);
            return this;
        }

        public ClientConfiguration build() {
            jaxrsConfig.property(ClientProperties.READ_TIMEOUT, socketTimeoutMs);
            jaxrsConfig.property(ClientProperties.CONNECT_TIMEOUT, connectTimeoutMs);
            jaxrsConfig.register(MultiPartFeature.class);
            jaxrsConfig.register(JaxbMessageReaderWriterProvider.class);
            jaxrsConfig.register(new AddRequestHeaderFilter(USER_AGENT, createUserAgentString()));
            for (LoggingFilter loggingFilter : this.loggingFilter) jaxrsConfig.register(loggingFilter);
            return new ClientConfiguration(keyStoreConfig, jaxrsConfig, globalSender, serviceRoot, certificatePaths);
        }

        String createUserAgentString() {
            return customUserAgentPart.map(inBetween("(", ")")).map(prepend(MANDATORY_USER_AGENT + " "))
                                      .orElse(MANDATORY_USER_AGENT);
        }

    }

}
