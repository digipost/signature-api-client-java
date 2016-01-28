package no.digipost.signature.client.core.internal;

import no.digipost.signature.client.ClientConfiguration;
import no.digipost.signature.client.core.exceptions.ConfigurationException;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public class TrustStoreBuilder {

    public static final String BUNDLED_CERTIFICATE_PATH = "/certificates/prod";
    public static final String BUNDLED_TEST_CERTIFICATE_PATH = "/certificates/test";

    public static KeyStore build(ClientConfiguration config) {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            List<String> certificateFolders = new ArrayList<>();
            certificateFolders.add(BUNDLED_CERTIFICATE_PATH);
            certificateFolders.add(BUNDLED_TEST_CERTIFICATE_PATH);

            for (String certificateFolder : certificateFolders) {
                loadCertificatesInto(certificateFolder, trustStore);
            }

            return trustStore;
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException | KeyManagementException e) {
            throw new ConfigurationException("Unable to load certificates into truststore", e);
        }
    }

    private static void loadCertificatesInto(String certificateFolder, final KeyStore trustStore) throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        new ClassPathFileLoader().forAllFilesInFolder(certificateFolder, new ForFile() {
            @Override
            void call(String fileName, InputStream contents) {
                try {
                    X509Certificate ca = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(contents);
                    trustStore.setCertificateEntry(fileName, ca);
                } catch (CertificateException | KeyStoreException e) {
                    throw new ConfigurationException("Unable to load certificate in " + fileName);
                }
            }
        });


        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);

        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, tmf.getTrustManagers(), null);
    }

    private static class ClassPathFileLoader {

        public void forAllFilesInFolder(String path, ForFile forEachFile) throws IOException {
            URL resourceUrl = TrustStoreBuilder.class.getResource(path);

            for (String fileName : IOUtils.toString(resourceUrl, Charsets.UTF_8).split("\n")) {
                InputStream contents = TrustStoreBuilder.class.getResourceAsStream(path + "/" + fileName);
                forEachFile.call(fileName, contents);
            }
        }
    }

    private abstract static class ForFile {
        abstract void call(String fileName, InputStream contents);
    }

}

