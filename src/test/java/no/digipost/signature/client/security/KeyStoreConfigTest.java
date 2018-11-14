package no.digipost.signature.client.security;

import no.digipost.signature.client.TestCertificates;
import org.junit.Test;

public class KeyStoreConfigTest {

    @Test
    public void can_load_from_organization_certificate() {
        KeyStoreConfig pkcs12KeyStore = TestCertificates.getOrganizationCertificateKeyStore();

        pkcs12KeyStore.getPrivateKey();
        pkcs12KeyStore.getCertificate();
        pkcs12KeyStore.getCertificateChain();
    }

    @Test
    public void can_load_from_java_key_store() {
        KeyStoreConfig javaKeyStore = TestCertificates.getJavaKeyStore();

        javaKeyStore.getPrivateKey();
        javaKeyStore.getCertificate();
        javaKeyStore.getCertificateChain();
    }

    @Test
    public void throws_exception_when_loading_from_empty_stream() {

    }
}
