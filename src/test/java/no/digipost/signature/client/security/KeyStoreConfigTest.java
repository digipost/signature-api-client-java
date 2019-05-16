package no.digipost.signature.client.security;

import no.digipost.signature.client.TestCertificates;
import no.digipost.signature.client.core.exceptions.KeyException;
import org.junit.jupiter.api.Test;

import static co.unruly.matchers.Java8Matchers.where;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class KeyStoreConfigTest {

    @Test
    public void can_load_from_organization_certificate() {
        KeyStoreConfig pkcs12KeyStore = TestCertificates.getOrganizationCertificateKeyStore();
        assertDoesNotThrow(pkcs12KeyStore::getPrivateKey);
        assertDoesNotThrow(pkcs12KeyStore::getCertificate);
        assertDoesNotThrow(pkcs12KeyStore::getCertificateChain);
    }

    @Test
    public void can_load_from_java_key_store() {
        KeyStoreConfig javaKeyStore = TestCertificates.getJavaKeyStore();
        assertDoesNotThrow(javaKeyStore::getPrivateKey);
        assertDoesNotThrow(javaKeyStore::getCertificate);
        assertDoesNotThrow(javaKeyStore::getCertificateChain);
    }

    @Test
    public void throws_exception_when_loading_organization_certificate_from_empty_stream() {
        KeyException thrown = assertThrows(KeyException.class, () -> KeyStoreConfig.fromOrganizationCertificate(null, "password;"));
        assertThat(thrown, where(Exception::getMessage, containsString("Please specify a stream with data")));
    }

    @Test
    public void throws_exception_when_loading_java_key_store_from_empty_stream() {
        KeyException thrown = assertThrows(KeyException.class, () -> KeyStoreConfig.fromJavaKeyStore(null, "alias", "password", "password"));
        assertThat(thrown, where(Exception::getMessage, containsString("Please specify a stream with data")));
    }
}
