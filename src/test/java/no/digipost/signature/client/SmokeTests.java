package no.digipost.signature.client;

import no.digipost.signature.client.core.Sender;
import no.digipost.signature.client.core.exceptions.KeyException;
import no.digipost.signature.client.direct.DirectClient;
import no.digipost.signature.client.direct.DirectDocument;
import no.digipost.signature.client.direct.DirectJob;
import no.digipost.signature.client.direct.DirectJobResponse;
import no.digipost.signature.client.direct.DirectSigner;
import no.digipost.signature.client.direct.ExitUrls;
import no.digipost.signature.client.security.KeyStoreConfig;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public class SmokeTests {

    private static final String BASE_API_URL = "https://localhost:8443";

    private DirectDocument getTestFile() throws IOException {

        InputStream documentStream = SmokeTests.class.getResourceAsStream("/document1.pdf");
        byte[] documentArray = IOUtils.toByteArray(documentStream);

        return DirectDocument.builder("Subject", "document.pdf", documentArray).message("Message").build();
    }

    private KeyStoreConfig keystore() {
        String keyStorePassword = System.getenv("ORG_KEY_STORE_PASSWORD");
        String privateKeyPassword = System.getenv("ORG_PRIVATE_KEY_PASSWORD");

        KeyStoreConfig keyStore;
        try {
            keyStore = KeyStoreConfig.fromJavaKeyStore(
                    getClass().getResourceAsStream("/client-keystore.jce"),
                    "posten norge as", keyStorePassword, privateKeyPassword
            );

        } catch (KeyException e) {
            throw new RuntimeException(
                    "To test the integration directly with Signering backend, please add the 'client-keystore.jce' from backend, " +
                            "and set key store and private key password as environment variables to avoid committing them:" +
                            "export ORG_KEY_STORE_PASSWORD={the keystore password, is in backend code}" +
                            "export ORG_PRIVATE_KEY_PASSWORD={the private key password, is in backend code}");
        }

        return keyStore;
    }

    private KeyStoreConfig pkcs12Keystore() {
        String privateKeyPassword = System.getenv("ORG_PRIVATE_KEY_PKCS12_BRING");
        KeyStoreConfig keyStoreConfig = KeyStoreConfig.fromOrganizationCertificate(getClass().getResourceAsStream("/Brig_Digital_Signature_Key_Encipherment_Data_Encipherment.p12"), privateKeyPassword);

        return keyStoreConfig;
    }

    private KeyStoreConfig originalKeystoreWithPkcs12() {
        String privateKeyPassword = System.getenv("ORG_PRIVATE_KEY_PKCS12_BRING");

        return KeyStoreConfig.fromJavaKeyStore(
                getClass().getResourceAsStream("/Bring_Digital_Signature_Key_Encipherment_Data_Encipherment.p12"),
                "digipost testintegrasjon for digital post", privateKeyPassword, privateKeyPassword
        );
    }

    @Test
    public void do_external_call() throws IOException {
        ClientConfiguration clientConfiguration = ClientConfiguration.builder(pkcs12Keystore())
                .globalSender(new Sender("988015814"))      //Bring
//                .globalSender(new Sender("984661185"))                    //Digipost
                .trustStore(Certificates.TEST)
                .serviceUri(ServiceUri.DIFI_TEST)
                .serviceUri(URI.create(BASE_API_URL + "/api"))
                .build();

        DirectClient directClient = new DirectClient(clientConfiguration);

        DirectDocument document = getTestFile();

        ExitUrls exitUrls = ExitUrls.of(
                "http://completion.example.com",
                "http://rejection.example.com",
                "http://error.example.com");
        DirectSigner signer = DirectSigner.withPersonalIdentificationNumber("01043100358").build();

        DirectJob signatureJob = DirectJob
                .builder(document, exitUrls, signer)
                .build();


        DirectJobResponse signatureJobResponse = directClient.create(signatureJob);

        System.out.println(signatureJobResponse.getSingleRedirectUrl());

    }
}
