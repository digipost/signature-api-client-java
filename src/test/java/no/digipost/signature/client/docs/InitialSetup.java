package no.digipost.signature.client.docs;

import no.digipost.signature.client.Certificates;
import no.digipost.signature.client.ClientConfiguration;
import no.digipost.signature.client.ServiceUri;
import no.digipost.signature.client.core.Sender;
import no.digipost.signature.client.security.KeyStoreConfig;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@SuppressWarnings("ConstantConditions")
public class InitialSetup {

    static void read_certificate_organization_certificate() throws FileNotFoundException {
        InputStream certificateStream = new FileInputStream("/Path/To/Certificate.p12");
        KeyStoreConfig keyStoreConfig = KeyStoreConfig.fromOrganizationCertificate(certificateStream, "passwordToCertificate");
    }

    static void read_certificate_java_key_store() throws FileNotFoundException {
        InputStream certificateStream = new FileInputStream("/Path/To/JavaKeyStore.jks");
        KeyStoreConfig keyStoreConfig = KeyStoreConfig.fromJavaKeyStore(
                certificateStream,
                "AliasOfOrganizationCertificate",
                "passwordToKeyStore",
                "passwordToCertificate"
        );
    }

    static void create_client_configuration() {
        KeyStoreConfig keyStoreConfig = null; //As initialized earlier

        ClientConfiguration clientConfiguration = ClientConfiguration.builder(keyStoreConfig)
                .trustStore(Certificates.TEST)
                .serviceUri(ServiceUri.DIFI_TEST)
                .globalSender(new Sender("123456789"))
                .build();

    }
}
