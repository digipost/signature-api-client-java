package no.digipost.signature.client.core.internal.security;

import no.digipost.signature.client.ClientConfiguration;
import no.digipost.signature.client.TestKonfigurasjon;
import org.junit.Before;
import org.junit.Test;

import java.security.KeyStore;
import java.security.KeyStoreException;

import static junit.framework.TestCase.assertEquals;
import static no.digipost.signature.client.Certificates.PRODUCTION;
import static no.digipost.signature.client.Certificates.TEST;

public class TrustStoreLoaderTest {

    private ClientConfiguration.Builder configBuilder;

    @Before
    public void setUp() {
        configBuilder = ClientConfiguration.builder(TestKonfigurasjon.CLIENT_KEYSTORE);
    }

    @Test
    public void loads_productions_certificates_by_default() throws KeyStoreException {
        KeyStore keyStore = TrustStoreLoader.build(configBuilder.build());

        assertEquals(4, keyStore.size());
        assertEquals("Trust store should contain bp root ca", true, keyStore.containsAlias("bpclass3rootca.cer"));
    }

    @Test
    public void loads_productions_certificates() throws KeyStoreException {
        ClientConfiguration config = configBuilder.trustStore(PRODUCTION).build();
        KeyStore keyStore = TrustStoreLoader.build(config);

        assertEquals(4, keyStore.size());
        assertEquals("Trust store should contain bp root ca", true, keyStore.containsAlias("bpclass3rootca.cer"));
        assertEquals("Trust store should not contain buypass test root ca", false, keyStore.containsAlias("buypass_class_3_test4_root_ca.cer"));
    }

    @Test
    public void loads_test_certificates() throws KeyStoreException {
        ClientConfiguration config = configBuilder.trustStore(TEST).build();
        KeyStore keyStore = TrustStoreLoader.build(config);

        assertEquals(5, keyStore.size());
        assertEquals("Trust store should not buypass root ca", false, keyStore.containsAlias("bpclass3rootca.cer"));
        assertEquals("Trust store should contain buypass test root ca", true, keyStore.containsAlias("buypass_class_3_test4_root_ca.cer"));
    }

    @Test
    public void loads_certificates_from_file_location() throws KeyStoreException {
        ClientConfiguration config = configBuilder.trustStore("./src/test/files/certificateTest").build();
        KeyStore keyStore = TrustStoreLoader.build(config);

        assertEquals(1, keyStore.size());
    }


}
