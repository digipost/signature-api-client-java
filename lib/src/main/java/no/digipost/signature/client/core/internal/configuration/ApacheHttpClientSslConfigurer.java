package no.digipost.signature.client.core.internal.configuration;

import no.digipost.signature.client.core.exceptions.KeyException;
import no.digipost.signature.client.core.internal.http.SignatureApiTrustStrategy;
import no.digipost.signature.client.core.internal.security.ProvidesCertificateResourcePaths;
import no.digipost.signature.client.core.internal.security.TrustStoreLoader;
import no.digipost.signature.client.security.CertificateChainValidation;
import no.digipost.signature.client.security.OrganizationNumberValidation;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.HostnameVerificationPolicy;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.core5.ssl.SSLContexts;

import javax.net.ssl.SSLContext;

public class ApacheHttpClientSslConfigurer implements Configurer<PoolingHttpClientConnectionManagerBuilder> {

    private ProvidesCertificateResourcePaths trustedCertificates;
    private CertificateChainValidation certificateChainValidation;

    public ApacheHttpClientSslConfigurer(ProvidesCertificateResourcePaths trustedCertificates) {
        this.trustedCertificates = trustedCertificates;
        this.certificateChainValidation = new OrganizationNumberValidation("984661185"); // Posten Bring AS organization number
    }

    public ApacheHttpClientSslConfigurer trust(ProvidesCertificateResourcePaths certificates) {
        this.trustedCertificates = certificates;
        return this;
    }

    public ApacheHttpClientSslConfigurer certificatChainValidation(CertificateChainValidation certificateChainValidation) {
        this.certificateChainValidation = certificateChainValidation;
        return this;
    }

    @Override
    public void applyTo(PoolingHttpClientConnectionManagerBuilder connectionManager) {
        connectionManager.setTlsSocketStrategy(
                new DefaultClientTlsStrategy(sslContext(), HostnameVerificationPolicy.CLIENT, NoopHostnameVerifier.INSTANCE));
    }


    private SSLContext sslContext() {
        try {
            return SSLContexts.custom()
                    .loadTrustMaterial(TrustStoreLoader.build(trustedCertificates), new SignatureApiTrustStrategy(certificateChainValidation))
                    .build();
        } catch (Exception e) {
            throw new KeyException("Unable to create the SSLContext, because " + e.getClass().getSimpleName() + ": '" + e.getMessage() + "'", e);
        }
    }
}
