package no.digipost.signature.client.security;

import no.digipost.signature.client.TestCertificates;
import no.digipost.signature.client.core.exceptions.KeyException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class KeyStoreConfigTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

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
    public void throws_exception_when_loading_organization_certificate_from_empty_stream() {
        expectedException.expect(KeyException.class);
        expectedException.expectMessage("Please specify a stream with data");
        KeyStoreConfig.fromOrganizationCertificate(null, "password;");
    }

    @Test
    public void throws_exception_when_loading_java_key_store_from_empty_stream() {
        expectedException.expect(KeyException.class);
        expectedException.expectMessage("Please specify a stream with data");
        KeyStoreConfig.fromJavaKeyStore(null, "alias", "password", "password");
    }
}
