package no.digipost.signature.client.core.internal;

import no.digipost.signature.client.ClientConfiguration;
import no.digipost.signature.client.TestKonfigurasjon;
import no.digipost.signature.client.core.Sender;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.security.KeyStore;
import java.security.KeyStoreException;

import static junit.framework.TestCase.assertEquals;
import static no.digipost.signature.client.ClientConfiguration.Certificates.PRODUCTION;
import static no.digipost.signature.client.ClientConfiguration.Certificates.TEST_AND_PRODUCTION;

public class TrustStoreBuilderTest {

    private ClientConfiguration.Builder configBuilder;

    @Before
    public void setUp() {
        configBuilder = ClientConfiguration.builder(URI.create("https://dummy.endpoint.no"), TestKonfigurasjon.CLIENT_KEYSTORE, new Sender("984661185"));
    }

    @Test
    public void loads_productions_certificates_by_default() throws KeyStoreException {
        KeyStore keyStore = TrustStoreBuilder.build(configBuilder.build());

        assertEquals(4, keyStore.size());
        assertEquals("Trust store should contain bp root ca", true, keyStore.containsAlias("bpclass3rootca.cer"));
    }

    @Test
    public void loads_productions_certificates() throws KeyStoreException {
        ClientConfiguration config = configBuilder.trustStore(PRODUCTION).build();
        KeyStore keyStore = TrustStoreBuilder.build(config);

        assertEquals(4, keyStore.size());
        assertEquals("Trust store should contain bp root ca", true, keyStore.containsAlias("bpclass3rootca.cer"));
        assertEquals("Trust store should not contain buypass test root ca", false, keyStore.containsAlias("buypass_class_3_test4_root_ca.cer"));
    }

    @Test
    public void loads_test_and_production_certificates() throws KeyStoreException {
        ClientConfiguration config = configBuilder.trustStore(TEST_AND_PRODUCTION).build();
        KeyStore keyStore = TrustStoreBuilder.build(config);

        assertEquals(9, keyStore.size());
        assertEquals("Trust store should contain buypass root ca", true, keyStore.containsAlias("bpclass3rootca.cer"));
        assertEquals("Trust store should contain buypass test root ca", true, keyStore.containsAlias("buypass_class_3_test4_root_ca.cer"));
    }

    @Test
    public void loads_certificates_from_file_location() throws KeyStoreException {
        ClientConfiguration config = configBuilder.trustStore("./src/test/files/certificateTest").build();
        KeyStore keyStore = TrustStoreBuilder.build(config);

        assertEquals(1, keyStore.size());
    }


}