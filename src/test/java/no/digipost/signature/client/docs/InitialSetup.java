package no.digipost.signature.client.docs;

import no.digipost.signature.client.ClientConfiguration;
import no.digipost.signature.client.core.Sender;
import no.digipost.signature.client.security.KeyStoreConfig;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import static no.digipost.signature.client.ServiceEnvironment.DIFITEST;

@SuppressWarnings({"ConstantConditions", "unused", "UnusedAssignment"})
public class InitialSetup {

    static void read_certificate_organization_certificate() throws IOException {
        KeyStoreConfig keyStoreConfig;
        try (InputStream certificateStream = Files.newInputStream(Paths.get("/path/to/certificate.p12"))) {
            keyStoreConfig = KeyStoreConfig.fromOrganizationCertificate(
                    certificateStream, "CertificatePassword"
            );
        }
    }

    static void read_certificate_java_key_store() throws IOException {
        KeyStoreConfig keyStoreConfig;
        try (InputStream certificateStream = Files.newInputStream(Paths.get("/path/to/javakeystore.jks"))) {
            keyStoreConfig = KeyStoreConfig.fromJavaKeyStore(
                    certificateStream,
                    "OrganizationCertificateAlias",
                    "KeyStorePassword",
                    "CertificatePassword"
            );
        }
    }

    static void create_client_configuration() {
        KeyStoreConfig keyStoreConfig = null; //As initialized earlier

        ClientConfiguration clientConfiguration = ClientConfiguration.builder(keyStoreConfig)
                .serviceEnvironment(DIFITEST)
                .defaultSender(new Sender("123456789"))
                .build();

    }
}
