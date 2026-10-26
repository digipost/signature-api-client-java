package no.digipost.signature.client.core.internal.configuration;

import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.TlsSocketStrategy;

/**
 * Standard TLS for using the API with OAuth: hostname verification, the JVM's default trust store,
 * and no client certificate, which is only presented to the token endpoint.
 *
 * @see ApacheHttpClientOrganizationCertificateTlsConfigurer
 */
public final class ApacheHttpClientStandardTlsConfigurer implements Configurer<PoolingHttpClientConnectionManagerBuilder> {

    /**
     * Resolved when created, which is when the JVM's default trust store is read.
     */
    private final TlsSocketStrategy standardTls = DefaultClientTlsStrategy.createDefault();

    @Override
    public void applyTo(PoolingHttpClientConnectionManagerBuilder connectionManager) {
        connectionManager.setTlsSocketStrategy(standardTls);
    }

}
