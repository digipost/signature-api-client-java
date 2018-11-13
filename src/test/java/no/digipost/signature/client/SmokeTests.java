package no.digipost.signature.client;

import no.digipost.signature.client.core.Sender;
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

        return KeyStoreConfig.fromKeyStore(
                getClass().getResourceAsStream("/client-keystore.jce"),
                "posten norge as", keyStorePassword, privateKeyPassword
        );


    }

    @Test
    public void do_external_call() throws IOException {
        ClientConfiguration clientConfiguration = ClientConfiguration.builder(keystore())
                .globalSender(new Sender("984661185"))
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

//        SignatureJobStatusResponse status = directClient.getStatus(new SignatureJobReference(signatureJobResponse.getStatusUrl()));
//        InputStream pAdES = directClient.getPAdES(status.getpAdESUrl());
//        IOUtils.copy(pAdES, new FileOutputStream("/tmp/pades.pdf"));

    }
}
