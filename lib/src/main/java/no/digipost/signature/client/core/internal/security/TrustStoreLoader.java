package no.digipost.signature.client.core.internal.security;

import no.digipost.signature.client.core.exceptions.ConfigurationException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.nio.file.Files.isDirectory;

public class TrustStoreLoader {

    public static KeyStore build(ProvidesCertificateResourcePaths trustedCertificates) {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            for (String certificateFolder : trustedCertificates.certificatePaths()) {
                loadCertificatesInto(certificateFolder, trustStore);
            }

            return trustStore;
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
            throw new ConfigurationException("Unable to load certificates into truststore", e);
        }
    }

    private static void loadCertificatesInto(String certificateLocation, KeyStore trustStore) {
        ResourceLoader certificateLoader;
        if (certificateLocation.startsWith(ClassPathResourceLoader.CLASSPATH_PATH_PREFIX)) {
            certificateLoader = new ClassPathResourceLoader(certificateLocation);
        } else {
            certificateLoader = new FileLoader(certificateLocation);
        }

        certificateLoader.forEachFile((fileName, contents) -> {
            X509Certificate ca = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(contents);
            trustStore.setCertificateEntry(fileName, ca);
        });

        try {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);

            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, tmf.getTrustManagers(), null);
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            throw new ConfigurationException("Error initializing SSLContext for certification location " + certificateLocation, e);
        }
    }


    private static class ClassPathResourceLoader implements ResourceLoader {

        static final String CLASSPATH_PATH_PREFIX = "classpath:";

        private final String resourceName;

        ClassPathResourceLoader(String resourceName) {
            this.resourceName = resourceName.replaceFirst(CLASSPATH_PATH_PREFIX, "");
        }

        @Override
        public void forEachFile(ForFile forEachFile) {
            URL resourceUrl = TrustStoreLoader.class.getResource(resourceName);
            if (resourceUrl == null) {
                throw new ConfigurationException(resourceName + " not found on classpath");
            }

            try (InputStream inputStream = resourceUrl.openStream()) {
                forEachFile.call(generateAlias(resourceName), inputStream);
            } catch (Exception e) {
                throw new ConfigurationException("Unable to load certificate from classpath: " + resourceName, e);
            }
        }
    }

    private static class FileLoader implements ResourceLoader {
        private final Path path;

        FileLoader(String certificateFolder) {
            this.path = Paths.get(certificateFolder);
        }

        @Override
        public void forEachFile(ForFile forEachFile) {
            if (!isDirectory(path)) {
                throw new ConfigurationException("Certificate path '" + this.path + "' is not a directory. " +
                        "It should point to a directory containing certificates.");
            }

            try (Stream<Path> files = Files.list(path)) {
                files.forEach(file -> {
                    try (InputStream contents = Files.newInputStream(file)) {
                        forEachFile.call(generateAlias(file), contents);
                    } catch (Exception e) {
                        throw new ConfigurationException("Unable to load certificate from file " + file, e);
                    }
                });
            } catch (IOException e) {
                throw new ConfigurationException("Error reading certificates from " + path, e);
            }
        }
    }

    private interface ResourceLoader {
        void forEachFile(ForFile forEachFile);
    }

    @FunctionalInterface
    private interface ForFile {
        void call(String fileName, InputStream contents) throws IOException, GeneralSecurityException;
    }

    static String generateAlias(Path location) {
        return generateAlias(location.toString());
    }

    private static final AtomicInteger aliasSequence = new AtomicInteger();

    static String generateAlias(String resourceName) {
        if (resourceName == null || resourceName.trim().isEmpty()) {
            return "certificate-alias-" + aliasSequence.getAndIncrement();
        }
        String[] splitOnSlashes = resourceName.split("/");
        int size = splitOnSlashes.length;
        if (size == 1) {
            return splitOnSlashes[0];
        } else {
            return splitOnSlashes[size - 2] + ":" + splitOnSlashes[size - 1];
        }
    }


}

