package no.digipost.signature.client;

import no.digipost.signature.client.security.BrokerId;
import no.digipost.signature.client.security.JwtAuthConfig;
import no.digipost.signature.client.security.KeyStoreConfig;


public class TestKonfigurasjon {

    public static final KeyStoreConfig CLIENT_KEYSTORE = KeyStoreConfig.fromJavaKeyStore(
            TestKonfigurasjon.class.getResourceAsStream("/selfsigned-keystore.jce"),
            "avsender",
            "password1234",
            "password1234"
    );

    public static final JwtAuthConfig JWT_AUTH_CONFIG = JwtAuthConfig.forClient("my-client-id", BrokerId.of("555444"));

}
