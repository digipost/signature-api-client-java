package no.digipost.signature.client;

import no.digipost.signature.client.security.KeyStoreConfig;

import java.io.IOException;
import java.io.InputStream;

public class TestCertificates {

    private static final String password = "yJPvczYAoirFfC9M";

    /**
     * Create Java Keystore from .p12-fil
     * <p>
     * Please note: The JKS keystore uses a proprietary format.
     * It is recommended to migrate to PKCS12 which is an industry standard format.
     * But if you want, this is how it is done for the Bring certificates used here:
     * <p>
     * <p>
     * keytool -importkeystore -srckeystore [MY_FILE.p12] -srcstoretype pkcs12
     * -srcalias [ALIAS_SRC] -destkeystore [MY_KEYSTORE.jks]
     * -deststoretype jks -deststorepass [PASSWORD_JKS] -destalias [ALIAS_DEST]
     * <p>
     * <p>
     * Example:
     * keytool -importkeystore -srckeystore Bring_Expired_Certificate_For_Testing.p12
     * -srcstoretype pkcs12 -srcalias 'digipost testintegrasjon for digital post'
     * -destkeystore bring-expired-keystore-for-testing.jks -deststoretype jks -deststorepass yJPvczYAoirFfC9M
     * -destalias 'digipost testintegrasjon for digital post'
     * <p>
     * The srcalias is often the friendly name of the certificate in lower case.
     */

    public static KeyStoreConfig getJavaKeyStore() {
        try (InputStream p12InputStream = TestCertificates.class.getResourceAsStream("/bring-expired-certificate-for-testing.p12")) {
            return KeyStoreConfig.fromOrganizationCertificate(p12InputStream, password);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static KeyStoreConfig getOrganizationCertificateKeyStore() {
        try (InputStream jksInputStream = TestCertificates.class.getResourceAsStream("/bring-expired-keystore-for-testing.jks")) {
            return KeyStoreConfig.fromJavaKeyStore(jksInputStream, "digipost testintegrasjon for digital post", password, password);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }


}
