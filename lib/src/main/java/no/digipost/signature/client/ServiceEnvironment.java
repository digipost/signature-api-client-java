package no.digipost.signature.client;

import no.digipost.signature.client.core.WithSignatureServiceRootUrl;
import no.digipost.signature.client.core.exceptions.ConfigurationException;
import no.digipost.signature.client.core.internal.security.ProvidesCertificateResourcePaths;
import no.digipost.signature.client.security.oauth.OAuthConfiguration;
import no.digipost.signature.client.security.oauth.Resource;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

/**
 * Available environments for integrating with Posten signering.
 */
public final class ServiceEnvironment implements ProvidesCertificateResourcePaths, WithSignatureServiceRootUrl {

    /**
     * Configuration for using OAuth, kept separate from organization certificate config.
     * The API uses a separate domain when using OAuth, with a standard TLS certificate issued for the domain name.
     */
    public static final class OAuthDefaults implements OAuthConfiguration.ServiceSpecific, WithSignatureServiceRootUrl {
        public static final OAuthDefaults PRODUCTION = new OAuthDefaults(
                URI.create("https://api.sign.digipost.no/api"),
                URI.create("https://midp.digipost.no/oauth2/token"),
                new Resource("https://api.sign.digipost.no"));

        public static final OAuthDefaults STAGING = new OAuthDefaults(
                URI.create("https://api.sign.difitest.digipost.no/api"),
                URI.create("https://midp.difitest.digipost.no/oauth2/token"),
                new Resource("https://api.sign.difitest.digipost.no"));

        private final URI serviceRootUrl;
        private final URI tokenEndpoint;
        private final Resource resource;

        private OAuthDefaults(URI serviceRootUrl, URI tokenEndpoint, Resource resource) {
            this.serviceRootUrl = serviceRootUrl;
            this.tokenEndpoint = tokenEndpoint;
            this.resource = resource;
        }

        @Override
        public String toString() {
            return "OAuth on API at " + serviceRootUrl + " with token endpoint " + tokenEndpoint + ", " + resource;
        }

        @Override
        public URI signatureServiceRootUrl() {
            return serviceRootUrl;
        }

        @Override
        public URI tokenEndpoint() {
            return tokenEndpoint;
        }

        @Override
        public Resource resource() {
            return resource;
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof OAuthDefaults) {
                OAuthDefaults that = (OAuthDefaults) other;
                return Objects.equals(this.serviceRootUrl, that.serviceRootUrl) &&
                        Objects.equals(this.tokenEndpoint, that.tokenEndpoint) &&
                        Objects.equals(this.resource, that.resource);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(serviceRootUrl, tokenEndpoint, resource);
        }
    }

    public static final ServiceEnvironment PRODUCTION = new ServiceEnvironment(
            "Posten signering Production", URI.create("https://api.signering.posten.no/api"), Certificates.PRODUCTION.certificatePaths,
            OAuthDefaults.PRODUCTION);

    public static final ServiceEnvironment STAGING = new ServiceEnvironment(
            "Posten signering Staging", URI.create("https://api.difitest.signering.posten.no/api"), Certificates.TEST.certificatePaths,
            OAuthDefaults.STAGING);

    public static final ServiceEnvironment DIFITEST = STAGING.withDescription("Posten signering Difitest");


    /**
     * @deprecated Difiqa is not operational anymore, use e.g. {@link #STAGING} instead
     */
    @Deprecated
    public static final ServiceEnvironment DIFIQA = new ServiceEnvironment(
            "Posten signering Difiqa", URI.create("https://api.difiqa.signering.posten.no/api"), Certificates.TEST.certificatePaths,
            null); // no token endpoint for this env


    private final String description;
    private final URI serviceRootUrl;
    private final List<String> certificatePaths;
    private final OAuthDefaults oAuth;


    /**
     * @deprecated This constructor produces an invalid ServiceEnvironment missing {@link #oAuth() OAuth defaults},
     *             and will be removed in the future. Refer to one of the predefined constants (e.g.
     *             {@link #STAGING}, {@link #PRODUCTION}), and customize them if needed, e.g. with
     *             {@link #withServiceUrl(URI)} and {@link #withOAuth(URI, URI, Resource)}.
     */
    @Deprecated
    public ServiceEnvironment(String description, URI serviceRootUrl, Collection<String> certificatePaths) {
        this(description, serviceRootUrl, certificatePaths, null);
    }

    private ServiceEnvironment(String description, URI serviceRootUrl, Collection<String> certificatePaths, OAuthDefaults oAuth) {
        this.description = description;
        this.serviceRootUrl = serviceRootUrl;
        this.certificatePaths = unmodifiableList(new ArrayList<>(certificatePaths));
        this.oAuth = oAuth;
    }

    /**
     * Use OAuth with a custom configuration. Only needed for custom environments.
     *
     * @param serviceRootUrl the API when using OAuth
     * @param tokenEndpointUrl the endpoint to acquire OAuth access tokens from
     * @param resource the resource to acquire access tokens for, which must match the token endpoint's configuration
     */
    public ServiceEnvironment withOAuth(URI serviceRootUrl, URI tokenEndpointUrl, Resource resource) {
        return new ServiceEnvironment(this.description, this.serviceRootUrl, this.certificatePaths, new OAuthDefaults(serviceRootUrl, tokenEndpointUrl, resource));
    }

    /**
     * Set the endpoint to acquire OAuth access tokens from, keeping the rest of the {@link #oAuth() OAuth configuration}.
     *
     * @see #withOAuth(URI, URI, Resource)
     */
    public ServiceEnvironment withTokenEndpoint(URI tokenEndpointUrl) {
        return withOAuth(oAuth().serviceRootUrl, tokenEndpointUrl, oAuth().resource);
    }

    public ServiceEnvironment withDescription(String description) {
        return new ServiceEnvironment(description, this.serviceRootUrl, this.certificatePaths, this.oAuth);
    }

    /**
     * Use a custom URL for the API when authenticating with the organization certificate.
     *
     * @see #withOAuth(URI, URI, Resource)
     */
    public ServiceEnvironment withServiceUrl(URI url) {
        return new ServiceEnvironment(this.description, url, this.certificatePaths, this.oAuth);
    }

    public ServiceEnvironment withAdditionalCertificates(String ... additionalCertificatePaths) {
        return withAdditionalCertificates(asList(additionalCertificatePaths));
    }

    public ServiceEnvironment withAdditionalCertificates(Collection<String> additionalCertificatePaths) {
        List<String> allCertificatePaths = new ArrayList<>(this.certificatePaths);
        allCertificatePaths.addAll(additionalCertificatePaths);
        return withCertificates(allCertificatePaths);
    }

    public ServiceEnvironment withCertificates(String ... certificatePaths) {
        return withCertificates(asList(certificatePaths));
    }

    public ServiceEnvironment withCertificates(Collection<String> certificatePaths) {
        return new ServiceEnvironment(this.description, this.serviceRootUrl, certificatePaths, this.oAuth);
    }

    /**
     * The API when authenticating with the organization certificate.
     *
     * @see OAuthDefaults#signatureServiceRootUrl()
     */
    @Override
    public URI signatureServiceRootUrl() {
        return serviceRootUrl;
    }

    /**
     * The trusted certificates apply only when authenticating with the organization certificate.
     * With OAuth, the API's certificate is validated against the JVM's default trust store.
     */
    @Override
    public List<String> certificatePaths() {
        return certificatePaths;
    }

    public OAuthDefaults oAuth() {
        if (oAuth == null) {
            throw new ConfigurationException(
                    this + " does not specify an OAuth configuration, which is expected for custom environments. " +
                    "Specify it with serviceEnvironment(env -> env.withOAuth(..)).");
        }
        return oAuth;
    }

    @Override
    public String toString() {
        return ServiceEnvironment.class.getSimpleName() + " '" + description + "', " +
                "using " + (oAuth != null ? oAuth + ", or " : "") +
                "mTLS directly on API at " + serviceRootUrl + " trusting " + certificatePaths;
    }

}


enum Certificates {

    TEST(
            "test/Buypass_Class_3_Test4_CA_3.cer",
            "test/Buypass_Class_3_Test4_Root_CA.cer",

            "test/BPCl3CaG2HTBS.cer",
            "test/BPCl3CaG2STBS.cer",
            "test/BPCl3RootCaG2HT.cer",
            "test/BPCl3RootCaG2ST.cer",

            "test/commfides_test_ca.cer",
            "test/commfides_test_root_ca.cer",

            "test/digipost_test_root_ca.cert.pem"

    ),
    PRODUCTION(
            "prod/BPClass3CA3.cer",
            "prod/BPClass3RootCA.cer",

            "prod/BPCl3CaG2HTBS.cer",
            "prod/BPCl3CaG2STBS.cer",
            "prod/BPCl3RootCaG2HT.cer",
            "prod/BPCl3RootCaG2ST.cer",

            "prod/commfides_ca.cer",
            "prod/commfides_root_ca.cer"
    );

    final List<String> certificatePaths;

    Certificates(String ... certificatePaths) {
        this.certificatePaths = Stream.of(certificatePaths)
                .map("classpath:/certificates/"::concat)
                .collect(toList());
    }

}

