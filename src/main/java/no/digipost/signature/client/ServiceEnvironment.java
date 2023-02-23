package no.digipost.signature.client;

import no.digipost.signature.client.core.internal.security.ProvidesCertificateResourcePaths;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

public final class ServiceEnvironment implements ProvidesCertificateResourcePaths {

    public static final ServiceEnvironment PRODUCTION = new ServiceEnvironment(
            "Posten Signering production", URI.create("https://api.signering.posten.no/api"), Certificates.PRODUCTION.certificatePaths);

    public static final ServiceEnvironment DIFITEST = new ServiceEnvironment(
            "Posten Signering difitest", URI.create("https://api.difitest.signering.posten.no/api"), Certificates.TEST.certificatePaths);

    public static final ServiceEnvironment DIFIQA = new ServiceEnvironment(
            "Posten Signering difiqa", URI.create("https://api.difiqa.signering.posten.no/api"), Certificates.TEST.certificatePaths);

    private final String description;
    private final URI serviceUrl;
    private final List<String> certificatePaths;


    public ServiceEnvironment(String description, URI serviceUrl, Collection<String> certificatePaths) {
        this.description = description;
        this.serviceUrl = serviceUrl;
        this.certificatePaths = unmodifiableList(new ArrayList<>(certificatePaths));
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
        return new ServiceEnvironment(this.description, this.serviceUrl, certificatePaths);
    }

    public URI serviceUrl() {
        return serviceUrl;
    }

    @Override
    public List<String> certificatePaths() {
        return certificatePaths;
    }



    @Override
    public String toString() {
        return description + " at " + serviceUrl + ", trusting " + certificatePaths;
    }

}


enum Certificates implements ProvidesCertificateResourcePaths {

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

    @Override
    public List<String> certificatePaths() {
        return certificatePaths();
    }

}

