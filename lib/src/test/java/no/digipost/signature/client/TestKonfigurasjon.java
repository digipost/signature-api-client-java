package no.digipost.signature.client;

import no.digipost.signature.client.security.KeyStoreConfig;


public class TestKonfigurasjon {

    public static final KeyStoreConfig CLIENT_KEYSTORE = KeyStoreConfig.fromJavaKeyStore(
            TestKonfigurasjon.class.getResourceAsStream("/selfsigned-keystore.jce"),
            "avsender",
            "password1234",
            "password1234"
    );


}
