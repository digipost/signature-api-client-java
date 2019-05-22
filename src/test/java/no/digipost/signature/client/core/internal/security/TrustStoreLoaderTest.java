package no.digipost.signature.client.core.internal.security;

import no.digipost.signature.client.ClientConfiguration;
import no.digipost.signature.client.TestKonfigurasjon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.KeyStore;
import java.security.KeyStoreException;

import static no.digipost.signature.client.Certificates.PRODUCTION;
import static no.digipost.signature.client.Certificates.TEST;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TrustStoreLoaderTest {

    private ClientConfiguration.Builder configBuilder;

    @BeforeEach
    public void setUp() {
        configBuilder = ClientConfiguration.builder(TestKonfigurasjon.CLIENT_KEYSTORE);
    }

    @Test
    public void loads_productions_certificates_by_default() throws KeyStoreException {
        KeyStore keyStore = TrustStoreLoader.build(configBuilder.build());

        assertThat(keyStore.size(), is(4));
        assertTrue(keyStore.containsAlias("bpclass3rootca.cer"), "Trust store should contain BuyPass root CA");
    }

    @Test
    public void loads_productions_certificates() throws KeyStoreException {
        ClientConfiguration config = configBuilder.trustStore(PRODUCTION).build();
        KeyStore keyStore = TrustStoreLoader.build(config);

        assertThat(keyStore.size(), is(4));
        assertTrue(keyStore.containsAlias("bpclass3rootca.cer"), "Trust store should contain bp root ca");
        assertFalse(keyStore.containsAlias("buypass_class_3_test4_root_ca.cer"), "Trust store should not contain buypass test root ca");
    }

    @Test
    public void loads_test_certificates() throws KeyStoreException {
        ClientConfiguration config = configBuilder.trustStore(TEST).build();
        KeyStore keyStore = TrustStoreLoader.build(config);

        assertThat(keyStore.size(), is(5));
        assertFalse(keyStore.containsAlias("bpclass3rootca.cer"), "Trust store should not buypass root ca");
        assertTrue(keyStore.containsAlias("buypass_class_3_test4_root_ca.cer"), "Trust store should contain buypass test root ca");
    }

    @Test
    public void loads_certificates_from_file_location() throws KeyStoreException {
        ClientConfiguration config = configBuilder.trustStore("./src/test/files/certificateTest").build();
        KeyStore keyStore = TrustStoreLoader.build(config);

        assertThat(keyStore.size(), is(1));
    }


}
