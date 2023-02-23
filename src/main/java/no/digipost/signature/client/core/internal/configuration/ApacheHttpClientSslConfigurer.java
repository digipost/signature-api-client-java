package no.digipost.signature.client.core.internal.configuration;

import no.digipost.signature.client.core.exceptions.KeyException;
import no.digipost.signature.client.core.internal.http.SignatureApiTrustStrategy;
import no.digipost.signature.client.core.internal.security.ProvidesCertificateResourcePaths;
import no.digipost.signature.client.core.internal.security.TrustStoreLoader;
import no.digipost.signature.client.security.CertificateChainValidation;
import no.digipost.signature.client.security.KeyStoreConfig;
import no.digipost.signature.client.security.OrganizationNumberValidation;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.ssl.SSLContexts;

import javax.net.ssl.SSLContext;

import java.security.UnrecoverableKeyException;

public class ApacheHttpClientSslConfigurer implements Configurer<PoolingHttpClientConnectionManagerBuilder> {

    private final KeyStoreConfig keyStoreConfig;
    private final ProvidesCertificateResourcePaths certificates;
    private CertificateChainValidation certificateChainValidation;

    public ApacheHttpClientSslConfigurer(KeyStoreConfig keyStoreConfig, ProvidesCertificateResourcePaths certificates) {
        this.keyStoreConfig = keyStoreConfig;
        this.certificates = certificates;
        this.certificateChainValidation = new OrganizationNumberValidation("984661185"); // Posten Norge AS organization number
    }

    public ApacheHttpClientSslConfigurer certificatChainValidation(CertificateChainValidation certificateChainValidation) {
        this.certificateChainValidation = certificateChainValidation;
        return this;
    }

    @Override
    public void applyTo(PoolingHttpClientConnectionManagerBuilder connectionManager) {
        connectionManager.setSSLSocketFactory(SSLConnectionSocketFactoryBuilder.create()
                .setSslContext(sslContext())
                .setHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .build());
    }


    private SSLContext sslContext() {
        try {
            return SSLContexts.custom()
                    .loadKeyMaterial(keyStoreConfig.keyStore, keyStoreConfig.privatekeyPassword.toCharArray(), (aliases, socket) -> keyStoreConfig.alias)
                    .loadTrustMaterial(TrustStoreLoader.build(certificates), new SignatureApiTrustStrategy(certificateChainValidation))
                    .build();
        } catch (Exception e) {
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
}
