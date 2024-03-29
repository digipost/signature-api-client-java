package no.digipost.signature.client;

import no.digipost.signature.client.core.WithSignatureServiceRootUrl;
import no.digipost.signature.client.core.internal.security.ProvidesCertificateResourcePaths;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

/**
 * Available environments for integrating with Posten signering.
 */
public final class ServiceEnvironment implements ProvidesCertificateResourcePaths, WithSignatureServiceRootUrl {

    public static final ServiceEnvironment PRODUCTION = new ServiceEnvironment(
            "Posten signering Production", URI.create("https://api.signering.posten.no/api"), Certificates.PRODUCTION.certificatePaths);

    public static final ServiceEnvironment DIFITEST = new ServiceEnvironment(
            "Posten signering Difitest", URI.create("https://api.difitest.signering.posten.no/api"), Certificates.TEST.certificatePaths);

    public static final ServiceEnvironment DIFIQA = new ServiceEnvironment(
            "Posten signering Difiqa", URI.create("https://api.difiqa.signering.posten.no/api"), Certificates.TEST.certificatePaths);

    public static final ServiceEnvironment STAGING = DIFITEST.withDescription("Posten signering Staging");


    private final String description;
    private final URI serviceRootUrl;
    private final List<String> certificatePaths;


    public ServiceEnvironment(String description, URI serviceRootUrl, Collection<String> certificatePaths) {
        this.description = description;
        this.serviceRootUrl = serviceRootUrl;
        this.certificatePaths = unmodifiableList(new ArrayList<>(certificatePaths));
    }

    public ServiceEnvironment withDescription(String description) {
        return new ServiceEnvironment(description, this.serviceRootUrl, this.certificatePaths);
    }

    public ServiceEnvironment withServiceUrl(URI url) {
        return new ServiceEnvironment(this.description, url, this.certificatePaths);
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
        return new ServiceEnvironment(this.description, this.serviceRootUrl, certificatePaths);
    }

    @Override
    public URI signatureServiceRootUrl() {
        return serviceRootUrl;
    }

    @Override
    public List<String> certificatePaths() {
        return certificatePaths;
    }



    @Override
    public String toString() {
        return description + " at " + serviceRootUrl + ", trusting " + certificatePaths;
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

