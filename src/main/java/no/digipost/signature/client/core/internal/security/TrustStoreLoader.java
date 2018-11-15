package no.digipost.signature.client.core.internal.security;

import no.digipost.signature.client.Certificates;
import no.digipost.signature.client.core.exceptions.ConfigurationException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
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

public class TrustStoreLoader {

    public static KeyStore build(ProvidesCertificateResourcePaths hasCertificatePaths) {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            for (String certificateFolder : hasCertificatePaths.getCertificatePaths()) {
                loadCertificatesInto(certificateFolder, trustStore);
            }

            return trustStore;
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException | KeyManagementException e) {
            throw new ConfigurationException("Unable to load certificates into truststore", e);
        }
    }

    private static void loadCertificatesInto(String certificateFolder, final KeyStore trustStore) throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        ResourceLoader certificateLoader;
        if (certificateFolder.indexOf("classpath:") == 0) {
            certificateLoader = new ClassPathFileLoader(certificateFolder);
        } else {
            certificateLoader = new FileLoader(certificateFolder);
        }

        certificateLoader.forEachFile(new ForFile() {
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

    private static class ClassPathFileLoader implements ResourceLoader {

        static final String CLASSPATH_PATH_PREFIX = "classpath:";

        private final String certificatePath;

        ClassPathFileLoader(String certificateFolder) {
            this.certificatePath = certificateFolder.substring(CLASSPATH_PATH_PREFIX.length());
        }

        @Override
        public void forEachFile(ForFile forEachFile) throws IOException {
            URL contentsUrl = Certificates.class.getResource(certificatePath);

            try (InputStream inputStream = contentsUrl.openStream()){
                forEachFile.call(new File(contentsUrl.getFile()).getName(), inputStream);
            }
        }
    }

    private static class FileLoader implements ResourceLoader {
        private final File path;

        FileLoader(String certificateFolder) {
            this.path = new File(certificateFolder);
        }

        @Override
        public void forEachFile(ForFile forEachFile) throws IOException {
            if (!this.path.isDirectory()) {
                throw new ConfigurationException("Certificate path '" + this.path + "' is not a directory. " +
                        "It should point to a directory containing certificates.");
            }
            File[] files = this.path.listFiles();
            if (files == null) {
                throw new ConfigurationException("Unable to read certificates from '" + path + "'. Make sure it's the correct path.");
            }

            for (File file : files) {
                try (InputStream contents = new FileInputStream(file)) {
                    forEachFile.call(file.getName(), contents);
                }
            }
        }
    }

    private interface ResourceLoader {
        void forEachFile(ForFile forEachFile) throws IOException;
    }

    private abstract static class ForFile {
        abstract void call(String fileName, InputStream contents);
    }

}

